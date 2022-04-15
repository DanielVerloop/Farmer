package com.analysis.structures;

import com.analysis.util.Advice;

import java.util.List;

public class Rule {
    private Advice advice;//Used to distinguish
    private String className;
    private List<String> parameters;
    private String fieldName;
    private String compareValue;
    private String assertExpr;
    private String methodName;

    /**
     * Default Rule type
     * @param advice
     * @param className
     * @param parameters
     */
    public Rule(Advice advice, String className, List<String> parameters) {
        this.advice = advice;
        this.className = className;
        this.parameters = parameters;
    }

    /**
     * When rule type constructor
     * @param advice
     * @param className
     * @param parameters
     * @param methodName
     */
    public Rule(Advice advice, String className, List<String> parameters, String methodName) {
        this.advice = advice;
        this.className = className;
        this.parameters = parameters;
        this.methodName = methodName;
    }

    /**
     * Rule type for Then steps
     * @param advice
     * @param className
     * @param methodName method
     * @param parameters method params
     * @param compareValue
     * @param assertExpr
     */
    public Rule(Advice advice, String className, String methodName, List<String> parameters, String compareValue, String assertExpr) {
        this.advice = advice;
        this.className = className;
        this.methodName = methodName;
        this.parameters = parameters;
        this.compareValue = compareValue;
        this.assertExpr = assertExpr;
    }

    /**
     * Rule type for Then steps
     * @param advice
     * @param className
     * @param fieldName class Field name
     * @param compareValue
     * @param assertExpr
     */
    public Rule(Advice advice, String className, String fieldName, String compareValue, String assertExpr) {
        this.advice = advice;
        this.className = className;
        this.fieldName = fieldName;
        this.compareValue = compareValue;
        this.assertExpr = assertExpr;
    }

    public Advice getAdvice() {
        return advice;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getCompareValue() {
        return compareValue;
    }

    public String getAssertExpr() {
        return assertExpr;
    }
}
