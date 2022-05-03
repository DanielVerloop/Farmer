package com.analysis;

import com.analysis.structures.constructor.ConstructorPair;
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
     * Gets a list of all parameter combinations of constructors of {@code className}
     * @param className name of class
     * @return all combinations
     * TODO:check case of no parameter constructor
     */
    public List<List<Parameter>> getConstructors(String className) {
        CompilationUnit cu = className2CU.get(className);
        List<List<Parameter>> result = new ArrayList<>();

        //Get Lists of all constructor parameter combinations
        cu.getClassByName(className).get().getConstructors().stream().forEach(constructorDeclaration -> {
            List<Parameter> temp = new ArrayList<>();
            //Create list of parameters for constructor
            constructorDeclaration.getParameters().forEach(parameter -> temp.add(parameter));
            result.add(temp); //add to result
        });

        return result;
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
        cu.getClassByName(className).get().getMethods().stream().forEach(methodDeclaration -> {
            if (methodDeclaration.getType().toString().contains(type)) {
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
    public List<String> filterMethodsOnParams(String className, List<String> parameters) {
        CompilationUnit cu = className2CU.get(className);
        Set<String> set = new LinkedHashSet<>();//to only keep distinct values
        List<String> result = new ArrayList<>();

        //TODO: fine-tune filter accuracy
        cu.getClassByName(className).get().getMethods().stream().forEach(methodDeclaration -> {
            if (methodDeclaration.getParameters().size() >= parameters.size()) {
                set.add(methodDeclaration.getNameAsString());
                for (Parameter param : methodDeclaration.getParameters()) {
                    for (String type : parameters) {
                        if (param.getType().toString().equals(type)) {
                            set.add(methodDeclaration.getNameAsString());
                        }
                    }
                    if (Arrays.asList("int", "double").contains(param.getType())) {
                        set.add(methodDeclaration.getNameAsString());
                    }
                }
            }
        });
        //return a list
        result.addAll(set);
        return result;
    }

    /**
     * Simplifies constructor parameters to native types
     * @param className
     * @return a list of all possible combinations of native type parameters
     */
    public List<ConstructorPair> constructorParamResolver(String className) {
        Set<ConstructorPair> result = new HashSet<>();

        for (ConstructorDeclaration constructorDeclaration : className2CU.get(className).getClassByName(className).get().getConstructors()) {
            if (constructorDeclaration.getParameters().size() == 0) {//handle no param constructors
                result.add(
                        new ConstructorPair(constructorDeclaration.getParameters(),
                                constructorDeclaration.getParameters())
                );
            }
            List<Parameter> parameters = constructorDeclaration.getParameters();
            for (Parameter parameter : parameters) {
                List<Parameter> finalParams = new ArrayList<>();//fix for concurrency errors
                finalParams.addAll(parameters);
                if (classNames.contains(parameter.getTypeAsString())) {//parameter is of custom class type
                    int index = finalParams.indexOf(parameter);//index of original parameter
                    for (ConstructorDeclaration constructorDeclaration1 :
                            className2CU.get(parameter.getTypeAsString()).getClassByName(parameter.getTypeAsString())
                                    .get().getConstructors()) {
                        List<Parameter> parameters2 = constructorDeclaration1.getParameters();
                        if (parameters2.size() == 0) {//empty constructor
                            result.add(new ConstructorPair(finalParams, parameters));//add with original type
                        } else { //a constructor with parameters, add them all to list
                            finalParams.remove(index);//remove original parameter
                            finalParams.addAll(index, parameters2);
                            //prevent doubles
//                            if(!result.contains(finalParams)) result.add(new ConstructorPair(finalParams, parameters));
                        }
                    }
                } else {//if other type
                    //set will take care of duplicates
                    result.add(
                            new ConstructorPair(constructorDeclaration.getParameters(),
                                    constructorDeclaration.getParameters())
                    );
                }
            }
        }
        List<ConstructorPair> returnList = new ArrayList<>();
        returnList.addAll(result);
        return returnList;
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
}
