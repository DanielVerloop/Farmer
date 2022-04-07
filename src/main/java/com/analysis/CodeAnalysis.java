package com.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeAnalysis {
    private HashMap<String, List<String>> mapMethods2Classes = new HashMap<>();
    private HashMap<String, List<String>> classFields = new HashMap<>();
    private File[] listFiles;
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
        this.listFiles = dir.listFiles();
        ArrayList<String> classNames = new ArrayList<>();
        for (File file: dir.listFiles()) {
                classNames.addAll(getClassNames(file));
                mapClass2CU(file, classNames);
        }
        this.classNames = classNames;
        for (File file : dir.listFiles()) {
            mapMethods2Classes(file, classNames);
        }
        for (File file : dir.listFiles()) {
            mapClassFields(file);
        }
    }

    private void mapClass2CU(File file, ArrayList<String> classNames) throws FileNotFoundException {
        for (String name : classNames) {
            CompilationUnit cu = StaticJavaParser.parse(file);
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

        return null;
    }
}
