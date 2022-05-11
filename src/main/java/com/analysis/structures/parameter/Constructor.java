package com.analysis.structures.parameter;

import java.util.Map;

public class Constructor {
    private String name;
    private Map<String, String> parameters;

    public Constructor(String name, Map<String, String> parameters) {
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
