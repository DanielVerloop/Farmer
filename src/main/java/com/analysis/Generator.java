package com.analysis;

import com.analysis.structures.Rule;
import com.analysis.structures.Scenario;
import com.analysis.structures.steps.Step;
import com.analysis.util.ParameterTester;
import com.analysis.util.StringFormatter;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.WrongWordspaceTypeException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


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
    public void generate() throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        NLPFileReader jsonResult = new NLPFileReader("src/main/resources/nlp_results.json",
                "src/test/resources/features/vendingMachine.feature");
//        "src/returnNumberType/resources/features/BankAccount.feature");
        File targetDir = new File("src/main/java/com/vendingMachine");
//        File targetDir = new File("src/main/java/com/bank");
        this.cu = new CompilationUnit();
        this.className = "vmStepDefs";
//        this.className = "bankStepDefs";

        //Get setMatchResult info
        List<Scenario> matchResult = new DistanceMatcher(
                targetDir, jsonResult.getScenarios("vendingMachine.feature")).getMatch();
//        List<Scenario> matchResult = new DistanceMatcher(
//                targetDir, jsonResult.getScenarios("transactions.feature")).getMatch();

        //Create skeleton template
        this.createTemplate(className);
        //fill method bodies
        this.addImplementation(matchResult);

        //output to file
        File file = new File("src/main/resources" + "/" + className + ".java");
        file.createNewFile();
        FileWriter fw = new FileWriter(file);
        fw.write(this.getCU().toString());
        fw.close();
    }

    /**
     * Add step function implementations of a single step file
     * @param matchResult
     */
    private void addImplementation(List<Scenario> matchResult) {
        CompilationUnit cu = getCU();
        ParameterTester parameterTester = new ParameterTester();

        ClassOrInterfaceDeclaration declaration = cu.getClassByName(className).get();
        for (Scenario scenario : matchResult) {
            List<Step> steps = getAllSteps(scenario.getSteps());
            for (Step step : steps) {
                String[] annotation = step.getDescription().split("\\s", 2);
                String name = new StringFormatter().camelCase(annotation[1]);

                // create the step method
                MethodDeclaration method;
                if (step.getNumbers().size() > 0) {
                    List<String> numbers = step.getNumbers();
                    for (int i = 0; i < numbers.size(); i++) {
                        name = name.replace(numbers.get(i), "Arg"+i);
                    }
                    method = declaration.addMethod(name, Modifier.Keyword.PUBLIC);
                    for (int i = 0; i < numbers.size(); i++) {
                        String type = parameterTester.returnNumberType(numbers.get(i));
                        method.addParameter(type, "arg"+i);
                        annotation[1] = annotation[1].replace(numbers.get(i), "{"+type+"}");
                    }
                } else {
                    method = declaration.addMethod(name, Modifier.Keyword.PUBLIC);
                }
                if (step.getParameters().size() > 0) {
                    for (String param : step.getParameters()) {
                        method.addParameter(step.getParent().getTypeSolver().getParameterType(param), param);
                        String varType = "{"+step.getParent().getTypeSolver().getParameterType(param).toLowerCase()+"}";
                        annotation[1] = annotation[1].replace(param, varType);
                    }
                    method.addSingleMemberAnnotation(annotation[0], new StringLiteralExpr(annotation[1]));
                } else {
                    method.addSingleMemberAnnotation(annotation[0], new StringLiteralExpr(annotation[1]));
                }

                //Check if we already created this method based on its name and annotation
                //If it exists, we remove the method we created
                if (cu.getClassByName(className).get().getMethodsByName(method.getNameAsString()).size() > 1
                    && cu.getClassByName(className).get().getMethodsByName(method.getNameAsString())
                        .get(0).getAnnotation(0).equals(method.getAnnotation(0))) {
                    cu.getClassByName(className).get().remove(method);
                    continue;
                }

                Rule info = step.getMatchResult();
                BlockStmt block;
                switch (info.getAdvice()) { //switch on first parameter
                case OBJECTI:
                    String objectName = info.getClassName();
                    String varName = new StringFormatter().camelCase(objectName);
                    String params = new StringFormatter().parseParameters(info.getParameters());
                    //code block variable and add it to the method
                    block = new BlockStmt();
                    method.setBody(block);

                    //add code to code block
                    ExpressionStmt stmt = new ExpressionStmt();
                    AssignExpr assignExpr;
                    if (params == null) {
                        assignExpr = new AssignExpr(
                                new NameExpr(varName),
                                new NameExpr("new " + objectName +"()"),
                                AssignExpr.Operator.ASSIGN
                        );
                    } else if (info.getMethodName() == null) {
                        assignExpr = new AssignExpr(
                                new NameExpr(varName),
                                new NameExpr("new " + objectName +"()"),
                                AssignExpr.Operator.ASSIGN
                        );
                    } else {
                        assignExpr = new AssignExpr(
                                new NameExpr(varName),
                                new NameExpr("new " + objectName +"(" + params + ")"),
                                AssignExpr.Operator.ASSIGN
                        );
                    }
                    stmt.setExpression(assignExpr);
                    block.addStatement(stmt);

                    //add field variable if it does not exist yet
                    this.addFieldToClass(objectName, varName);
                    break;
                case METHODI:
                    String var = new StringFormatter().camelCase(info.getClassName());

                    //code block variable and add it to the method
                    block = new BlockStmt();
                    method.setBody(block);

                    //add method call to block-statement
                    MethodCallExpr methodCallExpr = new MethodCallExpr(
                            new NameExpr(var),
                            info.getMethodName());
                    //handle method parameters
                    if (info.getParameters() != null && info.getParameters().size() > 0) {
                        for (String param : info.getParameters()) {
                            methodCallExpr.addArgument(param);
                        }
                    }
                    block.addStatement(methodCallExpr);
                    break;
                case ASSERT:
                    //code block variable and add it to the method
                    block = new BlockStmt();
                    method.setBody(block);
                    var = new StringFormatter().camelCase(info.getClassName());

                    //add assert call to block-statement
                    MethodCallExpr assertCallExpr = new MethodCallExpr(
                            new NameExpr("Assert"),
                            "assertTrue");
                    String assertCompareVal;
                    if (info.getParameters() != null &&
                            info.getParameters().contains(info.getCompareValue())) {//if we compare parameters
                        String compareType = step.getParent().getTypeSolver().getParameterType(info.getCompareValue());
                        if (compareType == "String") {
                            assertCompareVal = ".equals(" + info.getCompareValue() + ")";
                        } else {
                            assertCompareVal = " " + info.getCompareValue();
                        }
                    } else {
                        assertCompareVal = info.getCompareValue();
                    }
                    if (info.getFieldName() != null && !info.getFieldName().equals("")) {
                        assertCallExpr.addArgument(
                                var + "."
                                + info.getFieldName() + " "
                                + this.operator(info.getAssertExpr()) + " "
                                + assertCompareVal
                        );
                    } else {
                        assertCallExpr.addArgument(
                                var + "."
                                + info.getMethodName() + "() "
                                + this.operator(info.getAssertExpr())
                                + assertCompareVal
                        );
                    }

                    block.addStatement(assertCallExpr);
                    break;
                case Pass: //not able to create code
                    block = new BlockStmt();
                    method.setBody(block);

                    block.addStatement("pass;");
                    break;
                }


            }
        }
    }

    /**
     * Gets all steps in the correct ordering
     * @param steps
     * @return Ordered list of all steps in a scenario
     */
    private List<Step> getAllSteps(List<Step> steps) {
        List<Step> result = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            result.add(steps.get(i));
            if (steps.get(i).getAndSteps() != null && steps.get(i).getAndSteps().size() > 0) {
                for (Step andStep : steps.get(i).getAndSteps()) {
                    result.add(andStep);
                }
            }
        }
        return result;
    }

    private String operator(String s) {
        switch (s) {
            case "equals":
                return "==";
            case "higher":
                return ">";
            case "lower":
                return "<";
            case "sequals":
                return "";
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
        //Add standard template of gherkin
        cu.addClass(className);
        cu.addImport("io.cucumber.java.en.Given");
        cu.addImport("io.cucumber.java.en.When");
        cu.addImport("io.cucumber.java.en.Then");
        cu.addImport("io.cucumber.java.en.And");
        cu.addImport("org.junit.Assert");

        this.setCU(cu);
    }

    /**
     * Generate all stepfiles
     * @param targetDir directory of target source code
     * @param analysisNLP location of json of nlp analysis (from src dir)
     */
    public void generate(File targetDir, NLPFileReader analysisNLP) {
        System.out.printf("Generating over directory %s%n:", targetDir.getPath());
        System.out.println();
    }

    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        new Generator().generate();
    }
}
