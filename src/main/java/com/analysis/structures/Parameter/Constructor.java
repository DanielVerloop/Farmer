package com.analysis.structures.Parameter;

import java.util.HashMap;
import java.util.Map;

public class Parameter {
    private String name;
    private Map<String, String> parameters = new HashMap<>();

    public Parameter(String name, Map<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
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
                "className='" + name + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
