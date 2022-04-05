package com.analysis;

import com.analysis.structures.Scenario;
import com.analysis.structures.steps.Step;
import com.analysis.util.Advice;
import com.analysis.util.StringFormatter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Generator {
    private CompilationUnit cu;
    private String className;

    public CompilationUnit getCU() {
        return cu;
    }

    public void setCU(CompilationUnit cu) {
        this.cu = cu;
    }

    /**
     * Test method only!
     * TODO: remove method
     * TODO: add multifile generation
     */
    public void generate() throws IOException {
        NLPFileReader jsonResult = new NLPFileReader("src/main/resources/nlp_results_new.json");
        File targetDir = new File("src/main/java/com/bank");
        cu = new CompilationUnit();
        className = "bankAccountStepDefs";
        File file = new File("src/main/resources" + "/" + className + ".java");
        file.createNewFile();

        //Get setMatchResult info
        List<Scenario> matchResult = new Matcher(targetDir, jsonResult.getScenarios("transactions.feature")).getMatch();
        //Create skeleton template
        this.createTemplate(className);
        //TODO:add empty step-functions
        //TODO:fill method bodies
        this.addImplementation(matchResult, "transactions.feature");

        //output to file
        FileWriter fw = new FileWriter(file);
        fw.write(this.getCU().toString());
        fw.close();
    }

    /**
     * Add step function implementations of a single step file
     * @param matchResult
     * @param fileName
     */
    private void addImplementation(List<Scenario> matchResult, String fileName) throws FileNotFoundException {
        CompilationUnit cu = getCU();
        CompilationUnit cuTest = StaticJavaParser.parse(new File("src/test/java/com/stepdefinitions/cucumber/MyStepdefs.java"));
        System.out.println(cuTest);

        //TODO: implement the new strucure
        ClassOrInterfaceDeclaration declaration = cu.getClassByName(className).get();
        for (Scenario scenario : matchResult) {
            for (Step step : scenario.getSteps()) {
                String[] annotation = step.getDescription().split("\\s", 2);
                String name = new StringFormatter().camelCase(annotation[1]);

                // create the step method
                MethodDeclaration method = declaration.addMethod(name, Modifier.Keyword.PUBLIC);
                method.addSingleMemberAnnotation(annotation[0], new StringLiteralExpr(annotation[1]));

                List<String> info = step.getMatchResult();
                BlockStmt block;
                switch (Advice.valueOf(info.get(0))) { //switch on first parameter
                case OBJECTI:
                    String objectName = info.get(1);
                    String varName = new StringFormatter().camelCase(objectName);
                    String params = info.get(2);
                    //code block variable and add it to the method
                    block = new BlockStmt();
                    method.setBody(block);

                    //add code to code block
                    ExpressionStmt stmt = new ExpressionStmt();
                    AssignExpr assignExpr = new AssignExpr(
                            new NameExpr(varName),
                            new NameExpr("new " + objectName +"(" + params + ")"),
                            AssignExpr.Operator.ASSIGN
                    );
                    stmt.setExpression(assignExpr);
                    block.addStatement(stmt);

                    //add field variable if it does not exist yet
                    this.addFieldToClass(objectName, varName);
                    break;
                case METHODI:
                    //TODO need more info for accurate variable
                    String var = new StringFormatter().camelCase(info.get(2));

                    //code block variable and add it to the method
                    block = new BlockStmt();
                    method.setBody(block);

                    //add method call to block-statement
                    MethodCallExpr methodCallExpr = new MethodCallExpr(
                            new NameExpr(var),
                            info.get(1));
                    methodCallExpr.addArgument(info.get(3));
                    block.addStatement(methodCallExpr);
                    break;
                case ASSERT:
                    //TODO: Get info from previous steps

                    //code block variable and add it to the method
                    block = new BlockStmt();
                    method.setBody(block);

                    //add assert call to block-statement
                    MethodCallExpr assertCallExpr = new MethodCallExpr(
                            new NameExpr("Assert"),
                            "assertTrue");
                    assertCallExpr.addArgument(
                            info.get(1) + " "
                                    + this.operator(info.get(3)) + " "
                                    + info.get(2)
                    );
                    block.addStatement(assertCallExpr);
                    break;
            }


            }
        }
    }

    private String operator(String s) {
        switch (s) {
            case "equals":
                return "==";
            case "higher":
                return ">";
            case "lower":
                return "<";
            default:
                return s;
        }
    }

    private void addFieldToClass(String objectName, String varName) {
        CompilationUnit cu = getCU();

        boolean exists = false;
        for (FieldDeclaration field: cu.getClassByName(className).get().getFields()) {
                if (field.getCommonType().toString().equals(objectName) &&
                    field.getVariables().get(0).getNameAsString().equals(varName)) {
                    exists = true;
                }
        }
        if (!exists) {
            cu.getClassByName(className).get().addField(objectName, varName);
        }
    }

    /**
     * Generate Gherkin template
     */
    private void createTemplate(String className) {
        CompilationUnit cu = getCU();//create empty ast object
        List<String> scenarioFunctions = new ArrayList<>();
        //Add standard template of gherkin
        cu.addClass(className);
        cu.addImport("io.cucumber.java.en.Given");
        cu.addImport("io.cucumber.java.en.When");
        cu.addImport("io.cucumber.java.en.Then");
        cu.addImport("io.cucumber.java.en.And");
        cu.addImport("org.junit.Assert");

//        //Add function for each description
//        for (String description : descriptions) {
//            String[] annotation = description.split("\\s", 2);
//            String name = new StringFormatter().camelCase(annotation[1]);
//            cu.getClassByName(className).get().addMethod(name, Modifier.Keyword.PUBLIC);
//            cu.getClassByName(className).get()
//                    .getMethodsByName(name).get(0)
//                    .addSingleMemberAnnotation(annotation[0], new StringLiteralExpr(annotation[1]));
//            scenarioFunctions.add(name);
//        }
        this.setCU(cu);
    }

    /**
     * Generate all stepfiles
     * @param targetDir directory of target source code
     * @param analysisNLP location of json of nlp analysis (from src dir)
     */
    public void generate(File targetDir, NLPFileReader analysisNLP) {
        System.out.printf("Generating for directory %s%n:", targetDir.getPath());
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        new Generator().generate();
    }
}
