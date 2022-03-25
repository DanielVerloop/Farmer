package com.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeAnalysis {
    private HashMap<String, List<String>> mapMethods2Classes = new HashMap<String, List<String>>();
    private File[] listFiles;

    public HashMap<String, List<String>> getMapMethods2Classes() {
        return mapMethods2Classes;
    }

    /**
     * Default
     */
    public CodeAnalysis() {     }

    /**
     * analyse a complete directory
     * @param dir path to directory
     */
    public CodeAnalysis(File dir) throws FileNotFoundException {
        this.listFiles = dir.listFiles();
        ArrayList<String> classNames = new ArrayList<>();
        for (File file: dir.listFiles()) {
                classNames.addAll(getClassNames(file));
        }
        for (File file : dir.listFiles()) {
            mapMethods2Classes(file, classNames);
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
//            System.out.println(cl.getConstructors().get(0).getParameters().get(0).getType());
        });
        mapMethods2Classes(file, names);
        return names;
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

        //TODO: only return public methods!
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
}
