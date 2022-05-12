package com.analysis;

import com.analysis.structures.parameter.DescriptionParameter;
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

    public Generator(String fileName) {
        this.className = fileName;
    }
    /**
     * Test method only!
     * TODO: remove method
     * TODO: add multifile generation
     */
    public void generate(String featurfile, String featureFileLocation, String projdir) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        NLPFileReader jsonResult = new NLPFileReader(
                "src/main/resources/nlp_results.json",
                featureFileLocation
        );
        File targetDir = new File(projdir);
        this.cu = new CompilationUnit();


        //Get setMatchResult info
        List<Scenario> matchResult = new DistanceMatcher(
                targetDir, jsonResult.getScenarios(featurfile)).getMatch();

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
                List<DescriptionParameter> descriptionParameters = new ArrayList<>();
                if (step.getNumbers().size() > 0) {
                    List<String> numbers = step.getNumbers();
                    for (int i = 0; i < numbers.size(); i++) {
                        name = name.replace(numbers.get(i), "Arg"+i);
                    }
                    method = declaration.addMethod(name, Modifier.Keyword.PUBLIC);

                    for (int i = 0; i < numbers.size(); i++) {
                        String type = parameterTester.returnNumberType(numbers.get(i));
                        descriptionParameters.add(new DescriptionParameter("arg"+i, type, numbers.get(i)));
                        annotation[1] = annotation[1].replace(numbers.get(i), "{"+type+"}");
                    }
                } else {
                    method = declaration.addMethod(name, Modifier.Keyword.PUBLIC);
                }
                if (step.getParameters().size() > 0) {
                    for (String param : step.getParameters()) {
                        String type = step.getParent().getTypeSolver().getParameterType(param);
                        descriptionParameters.add(new DescriptionParameter(param, type));
                        String varType = "{"+type.toLowerCase()+"}";
                        annotation[1] = annotation[1].replace(param, varType);
                    }
                    method.addSingleMemberAnnotation(annotation[0], new StringLiteralExpr(annotation[1]));
                } else {
                    method.addSingleMemberAnnotation(annotation[0], new StringLiteralExpr(annotation[1]));
                }
                //Parse parameters and add to method in correct order
                List<String> methodParams = new StringFormatter().orderParameters(step.getDescription(), descriptionParameters);
                for (String p: methodParams) {
                    String[] split = p.split("\\s");
                    method.addParameter(split[0], split[1]);
                }

                //Check if we already created this method based on its name and annotation
                //If it exists, we remove the method we created
                if (cu.getClassByName(className).get().getMethodsByName(method.getNameAsString()).size() > 1
                    && cu.getClassByName(className).get().getMethodsByName(method.getNameAsString())
                        .get(0).getAnnotation(0).equals(method.getAnnotation(0))) {
                    cu.getClassByName(className).get().remove(method);
                    continue;//go to next step
                }

                List<Rule> codeRules = step.getMatchResult();
                //code block variable and add it to the method
                BlockStmt block = new BlockStmt();
                method.setBody(block);
                for (Rule code : codeRules) {
                    switch (code.getAdvice()) { //switch on first parameter
                        case OBJECTI:
                            String objectName = code.getClassName();
                            String varName = new StringFormatter().camelCase(objectName);
                            String params = new StringFormatter().parseParameters(code.getParameters());

                            //add code to code block
                            ExpressionStmt stmt = new ExpressionStmt();
                            AssignExpr assignExpr;
                            if (params == null) {
                                assignExpr = new AssignExpr(
                                        new NameExpr(varName),
                                        new NameExpr("new " + objectName +"()"),
                                        AssignExpr.Operator.ASSIGN
                                );
                            } else if (code.getMethodName() == null) {
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
                            String var = new StringFormatter().camelCase(code.getClassName());

                            //add method call to block-statement
                            MethodCallExpr methodCallExpr = new MethodCallExpr(
                                    new NameExpr(var),
                                    code.getMethodName());
                            //handle method parameters
                            if (code.getParameters() != null && code.getParameters().size() > 0) {
                                for (String param : code.getParameters()) {
                                    methodCallExpr.addArgument(param);
                                }
                            }
                            block.addStatement(methodCallExpr);
                            break;
                        case ASSERT:
                            var = new StringFormatter().camelCase(code.getClassName());

                            //add assert call to block-statement
                            MethodCallExpr assertCallExpr = new MethodCallExpr(
                                    new NameExpr("Assert"),
                                    "assertTrue");
                            String assertCompareVal = null;
                            StringBuilder parameters = new StringBuilder();
                            //If parameters are used
                            if (code.getParameters() != null && code.getParameters().size() > 0) {
                                for (String param: code.getParameters()) {
                                    if (param.equals(code.getCompareValue())) {
                                        String compareType = step.getParent().getTypeSolver().getParameterType(code.getCompareValue());
                                        if (compareType.equals("String")) {
                                            assertCompareVal = ".equals(" + code.getCompareValue() + ")";
                                        } else {
                                            assertCompareVal = " " + code.getCompareValue();
                                        }
                                    } else {//add parameter to getter
                                        if (!parameters.toString().equals("")) {
                                            parameters.append(", ").append(param);
                                        } else {
                                            parameters.append(param);
                                        }
                                    }
                                }
                                //if no parameter is the desired value
                                if (assertCompareVal == null) {
                                    assertCompareVal = code.getCompareValue();
                                }
                            } else {
                                assertCompareVal = code.getCompareValue();
                            }
                            if (code.getFieldName() != null && !code.getFieldName().equals("")) {
                                assertCallExpr.addArgument(
                                        var + "."
                                                + code.getFieldName() + " "
                                                + this.operator(code.getAssertExpr()) + " "
                                                + assertCompareVal
                                );
                            } else {
                                assertCallExpr.addArgument(
                                        var + "."
                                                + code.getMethodName() + "(" + parameters + ") "
                                                + this.operator(code.getAssertExpr())
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


//    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
//        new Generator().generate();
//    }
}
