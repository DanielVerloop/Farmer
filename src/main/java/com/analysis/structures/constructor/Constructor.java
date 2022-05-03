package com.analysis.structures.constructor;

import java.util.HashMap;
import java.util.Map;

public class Constructor {
    private String className;
    private Map<String, String> parameters = new HashMap<>();

    public Constructor(String name, Map<String, String> parameters) {
        this.className = name;
        this.parameters = parameters;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameterType(String name) {
        return parameters.get(name);
    }

    @Override
    public String toString() {
        return "Constructor{" +
                "className='" + className + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
