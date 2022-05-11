package com.analysis;

import com.analysis.structures.parameter.Method;
import com.analysis.structures.parameter.ParameterPair;
import com.analysis.structures.parameter.ParameterTree;
import com.analysis.structures.parameter.ParameterTreeNode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CodeAnalysis {
    private HashMap<String, List<String>> mapMethods2Classes = new HashMap<>();
    private HashMap<String, List<String>> classFields = new HashMap<>();
    private List<String> classNames;
    private Map<String, CompilationUnit> className2CU = new HashMap<>();//TODO: make all methods use this for perfomance optimization

    public HashMap<String, List<String>> getMapMethods2Classes() {
        return mapMethods2Classes;
    }

    public HashMap<String, List<String>> getClassFields() {
        return classFields;
    }

    /**
     * analyse a complete directory
     * @param dir path to directory
     */
    public CodeAnalysis(File dir) throws FileNotFoundException {
        ArrayList<String> classNames = new ArrayList<>();
        for (File file: dir.listFiles()) {
                classNames.addAll(getClassNames(file));
                mapClass2CU(file);
        }
        this.classNames = classNames;
        for (File file : dir.listFiles()) {
            mapMethods2Classes(file, classNames);
        }
        for (File file : dir.listFiles()) {
            mapClassFields(file);
        }
    }

    private void mapClass2CU(File file) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(file);
        List<String> classNames = getClassNames(file);
        for (String name : classNames) {
            className2CU.put(name, cu);
        }
    }

    /**
     * Gets all names of all classes
     * @param file java file
     * @return Array containing all class names in a java file
     * @throws FileNotFoundException
     */
    public List<String> getClassNames(File file) throws FileNotFoundException {
        ArrayList<String> names = new ArrayList<>();

        // Parse some code
        CompilationUnit cu = StaticJavaParser.parse(file);
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cl -> {
            names.add(cl.getNameAsString());
        });
        mapMethods2Classes(file, names);
        return names;
    }

    public void mapClassFields(File file) throws FileNotFoundException {
        if (classNames.size() < 1) {
            throw new NullPointerException("No class names found");
        }

        CompilationUnit cu = StaticJavaParser.parse(file);
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cl -> {
            if (classNames.contains(cl.getNameAsString())) {
                List<String> classVars = new ArrayList<>();
                cl.getFields().forEach(fieldDeclaration -> {
                    if (fieldDeclaration.getAccessSpecifier().equals(AccessSpecifier.PUBLIC)) {
                        classVars.add(fieldDeclaration.getVariables().get(0).getNameAsString());
                    }
                });
                classFields.put(cl.getNameAsString(), classVars);
            }
        });

    }
    /**
     * maps classes to their methods
     * @param file
     * @param names allows for a restriction of the used classes
     * @throws FileNotFoundException
     */
    public void mapMethods2Classes(File file, List<String> names) throws FileNotFoundException {
        if (names.size() < 1) {
            throw new NullPointerException("names may not be empty");
        }

        CompilationUnit cu = StaticJavaParser.parse(file);
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cl -> {
            if (names.contains(cl.getNameAsString())) {
                List<String> methods = new ArrayList<>();
                cl.getMethods().forEach(methodDeclaration -> {
                    // Only add public methods to analysis
                    if (methodDeclaration.getAccessSpecifier().equals(AccessSpecifier.PUBLIC))
                        methods.add(methodDeclaration.getNameAsString());
                });
                mapMethods2Classes.put(cl.getNameAsString(), methods);
            }
        });
    }

    /**
     * Get all methods that are not of type void
     * @param className
     * @param type
     * @return
     */
    public List<String> getMethodsWithReturnType(String className, String type) {
        CompilationUnit cu = className2CU.get(className);
        List<String> result = new ArrayList<>();

        //get all methods that have a return type containing type
        //use lowercase to work around Double vs double kind of issues
        cu.getClassByName(className).get().getMethods().stream().forEach(methodDeclaration -> {
            if (methodDeclaration.getType().toString().toLowerCase().contains(type.toLowerCase())) {
                result.add(methodDeclaration.getNameAsString());
            }
        });

        return result;
    }

    /**
     * Reduces the method search space for a class
     * @param className
     * @param parameters list of parameter types
     * @return
     */
    public List<Method> filterMethodsOnParams(String className, List<String> parameters) {
        CompilationUnit cu = className2CU.get(className);
        Set<Method> set = new LinkedHashSet<>();//to only keep distinct values
        List<Method> result = new ArrayList<>();

        //TODO: fine-tune filter accuracy
        //TODO: allow for no filtering if parameters is empty
        cu.getClassByName(className).get().getMethods().stream().forEach(methodDeclaration -> {
            List<ParameterPair> pairs = reduceObjectParameters(methodDeclaration.getParameters());
            for (ParameterPair pair : pairs) {
                if (parameters == null) {//default behaviour add all options
                    set.add(new Method(methodDeclaration.getNameAsString(), pair));
                } else if (pair.getBaseParams().size() == parameters.size()) {
//                    set.add(new Method(methodDeclaration.getNameAsString(), pair));
                    for (Parameter p : pair.getBaseParams()) {
                        for (String type : parameters) {
                            if (p.getTypeAsString().equals(type)) {
                                set.add(new Method(methodDeclaration.getNameAsString(), pair));
                            }
                        }
                        if (Arrays.asList("int", "double").contains(p.getTypeAsString())) {
                            set.add(new Method(methodDeclaration.getNameAsString(), pair));
                        }
                    }
                }
            }

        });
        //return a list
        result.addAll(set);
        return result;
    }

    /**
     * Reduces list of parameters to only standard types (String, int, double, etc)
     * Custom class object parameters are reduced to their constructor parameters
     */
    private List<ParameterPair> reduceObjectParameters(List<Parameter> parameters) {
        List<ParameterPair> result = new ArrayList<>();//multiple constructors -> multiple choices
        ParameterTree tree = new ParameterTree();//tree with all choices
        for (Parameter parameter : parameters) {
            if (this.classNames.contains(parameter.getTypeAsString())) {
                tree.addNode(parseClassObject(parameter));
            } else {//standard type param
                ParameterTreeNode node = new ParameterTreeNode();
                node.addParameterList(Arrays.asList(parameter));
                tree.addNode(node);
            }
        }
        result = tree.createPairList(parameters);
        return result;
    }


    /**
     * Helper function to parse a constructor to parameter object
     */
    private ParameterTreeNode parseClassObject(Parameter parameter) {
        String name = parameter.getTypeAsString();
        ParameterTreeNode node = new ParameterTreeNode();
        for (ConstructorDeclaration constructorDeclaration : className2CU.get(name).getClassByName(name).get().getConstructors()) {
            node.addParameterList(constructorDeclaration.getParameters());
        }
        return node;
    }

    /**
     * Simplifies constructor parameters to native types
     * @param className
     * @return a list of all possible combinations of native type parameters
     */
    public List<ParameterPair> constructorParamResolver(String className) {
        Set<ParameterPair> result = new HashSet<>();

        for (ConstructorDeclaration constructorDeclaration : className2CU.get(className).getClassByName(className).get().getConstructors()) {
            if (constructorDeclaration.getParameters().size() == 0) {//handle no param constructors
                result.add(
                        new ParameterPair(constructorDeclaration.getParameters(),
                                constructorDeclaration.getParameters())
                );
            }
            List<Parameter> parameters = constructorDeclaration.getParameters();
            for (Parameter parameter : parameters) {
                if (classNames.contains(parameter.getTypeAsString())) {//parameter is of custom class type
                    result.addAll(reduceObjectParameter(parameters, parameter));
                } else {//if other type
                    //set will take care of duplicates
                    result.add(
                            new ParameterPair(constructorDeclaration.getParameters(),
                                    constructorDeclaration.getParameters())
                    );
                }
            }
        }
        List<ParameterPair> returnList = new ArrayList<>();
        returnList.addAll(result);
        return returnList;
    }

    /**
     * Method to reduce {@code parameter} from custom class type to only basic types (string, int, double, etc)
     * @param parameters
     * @param parameter
     * @return
     */
    private Set<ParameterPair> reduceObjectParameter(List<Parameter> parameters, Parameter parameter) {
        Set<ParameterPair> result = new HashSet<>();
        List<Parameter> finalParams = new ArrayList<>();//fix for concurrency errors
        finalParams.addAll(parameters);
        int index = finalParams.indexOf(parameter);//index of original parameter
        for (ConstructorDeclaration constructorDeclaration1 :
                className2CU.get(parameter.getTypeAsString()).getClassByName(parameter.getTypeAsString())
                        .get().getConstructors()) {
            List<Parameter> parameters2 = constructorDeclaration1.getParameters();
            if (parameters2.size() == 0) {//empty constructor
                result.add(new ParameterPair(parameters, parameters));//add with original type
            } else { //a constructor with parameters, add them all to list
                finalParams.remove(index);//remove original parameter
                finalParams.addAll(index, parameters2);
                List<Parameter> resultParams = new ArrayList<>();
                for (Parameter p : finalParams) resultParams.add(p);
                result.add(new ParameterPair(resultParams,parameters));
            }
        }
        return result;
    }

    /**
     * Checks if list of types match the parameter types of a constructor
     * @param resolvedConstructor
     * @param targetTypes
     * @return
     */
    public boolean checkParamTypes(List<Parameter> resolvedConstructor, List<String> targetTypes) {
        int count = 0;
        for (int i = 0; i < targetTypes.size(); i++) {
            for (int j = 0; j < resolvedConstructor.size(); j++) {
                if (targetTypes.get(i).equals(resolvedConstructor.get(j).getTypeAsString())) {
                    count++;
                }
            }
        }
        if (count == targetTypes.size()) {
            return true;
        }
        return false;
    }

    public List<String> getMethodsWithParamtype(String className, String paramType) {
        CompilationUnit cu = className2CU.get(className);
        Set<String> set = new LinkedHashSet<>();//to only keep distinct values
        List<String> result = new ArrayList<>();

        cu.getClassByName(className).get().getMethods().stream().forEach(methodDeclaration -> {
            for (Parameter param : methodDeclaration.getParameters()) {
                if (param.getTypeAsString().equals(paramType)) {
                    set.add(methodDeclaration.getNameAsString());
                }
            }
        });
        //return a list
        result.addAll(set);
        return result;
    }

    //for testing only!
    public static void main(String[] args) throws FileNotFoundException {
        CodeAnalysis analysis = new CodeAnalysis(new File("src/main/java/com/vendingmachine"));
        analysis.filterMethodsOnParams("VendingMachine", Arrays.asList("String", "int"));

    }
}

