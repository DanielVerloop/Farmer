package com.analysis;

import com.analysis.structures.Scenario;
import com.analysis.structures.steps.GivenStep;
import com.analysis.structures.steps.Step;
import com.analysis.util.StringFormatter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import info.debatty.java.stringsimilarity.Cosine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class UnitTestCrawler {
    private final List<String> classNames;
    private final JavaParserTypeSolver typesolver;
    private File testFile;
    private CompilationUnit cu;
    private static Cosine cosine = new Cosine();
    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public UnitTestCrawler(String filepath, List<String> classNames) throws FileNotFoundException {
        testFile = new File(filepath);
        cu = StaticJavaParser.parse(testFile);
        names = extractInfo();
        this.classNames = classNames;
        this.typesolver = new JavaParserTypeSolver("C:/Users/danielv/Documents/GitHub/CoffeeMachine/src/");
    }

    private ArrayList<String> extractInfo() {
        ArrayList<String> methodNames = new ArrayList<>();

        cu.findAll(MethodDeclaration.class).forEach(
                methodDeclaration -> methodNames.add(methodDeclaration.getNameAsString())
        );

        StringFormatter formatter = new StringFormatter();
        ArrayList<String> processedNames = new ArrayList<>();
        for (String name : methodNames) {
            processedNames.add(formatter.splitCamelCase(name));
        }

        return processedNames;
    }

    public Map<String, String> matchToSteps(List<String> steps, List<String> unitTestNames) {
        Map<String, String> result = new HashMap<>();
        for (String step : steps) {
            List<Double> distances = new ArrayList<>();
            for (String test : unitTestNames) {
                distances.add(cosine.distance(test, step));
            }

            int minIndex = distances.indexOf(Collections.min(distances));

            result.put(step, unitTestNames.get(minIndex));
        }

        return result;
    }

    public String matchToStep(String step) {
        List<String> unitTestNames = getNames();
        List<Double> distances = new ArrayList<>();
        for (String test : unitTestNames) {
            distances.add(cosine.distance(test, step));
        }

        int minIndex = distances.indexOf(Collections.min(distances));

        return unitTestNames.get(minIndex);
    }

    public List<String> getSetOfClasses(Scenario scenario) {
        //first build the description
        StringBuilder description = new StringBuilder();
        GivenStep givenStep = (GivenStep) scenario.getSteps().get(0);
        description.append(givenStep.getDescription());
        description.append(" ");
        for (Step step: givenStep.getAndSteps()) {
            description.append(step.getDescription());
            description.append(" ");
        }
        String finalStepDescription = description.toString().trim();

        String unitTest = matchToStep(finalStepDescription);

        StringFormatter formatter = new StringFormatter();
        String functionName = formatter.toCamelCase(unitTest);

        return extractClasses(functionName);
    }

    private List<String> extractClasses(String functionName) {
        MethodDeclaration unitTest;
        Set<String> classes = new HashSet<>();

        //Get the unitTest code
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMethodsByName(functionName);
            for (MethodDeclaration method: methods) {
                method.findAll(ObjectCreationExpr.class).forEach(objectCreationExpr -> {
                    String type = objectCreationExpr.getTypeAsString();
                    if (classNames.contains(type)) {
                        classes.add(type);
                    }
                });
            }
        }

        //return the result
        List<String> result = new ArrayList<>();
        result.addAll(classes);

        //If no unit-test was matched we want to prevent an empty set
        if (result.size() == 0) {
            result = classNames;
        }
        return result;
    }


    public static void main(String[] args) throws FileNotFoundException {
//        ArrayList<String> given = new ArrayList<>(Arrays.asList(
//                "Given there is a bar with a coffee machine that contains 5 beans and 3 milk",
//                "Given there is a bar And a coffee machine with 1 beans and 1 milk",
//                "Given there is a bar And the coffee machine contains 7 beans and 4 milk",
//                "Given there is a bar And the bar has a soda machine with <liters> of <soda>",
//                "Given there is a bar And the bar has an empty soda machine And the bar has an empty coffee machine"
//        ));
//        UnitTestCrawler crawler = new UnitTestCrawler(
//                "C:/Users/danielv/Documents/GitHub/CoffeeMachine/src/test/java/BarTest.java"
//        );
//        ArrayList<String> names = crawler.extractInfo();
//        Map<String, String> result = crawler.matchToStep(given, names);
//        result.forEach((s, s2) -> {
//            System.out.printf("Step: %s -----> matches to function: %s\n", s, s2);
//        });
    }

}
