package com.analysis.research;

import com.analysis.CodeAnalysis;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;

public class InheritenceTest {
    private CompilationUnit cu;

    public InheritenceTest(File file) throws FileNotFoundException {
        cu = StaticJavaParser.parse(file);
        System.out.println(cu);
    }


    public static void main(String[] args) throws FileNotFoundException {
//        InheritenceTest it = new InheritenceTest(
//                new File("C:/Users/danielv/Documents/GitHub/Farmer/src/main/java/com/analysis/structures/steps/GivenStep.java")
//        );

        CodeAnalysis codeAnalysis = new CodeAnalysis(new File("C:/Users/danielv/Documents/GitHub/Farmer/src/main/java/com/analysis/research/"));
        System.out.println(codeAnalysis.filterMethodsOnParams("SubSubClass", null));
    }
}
