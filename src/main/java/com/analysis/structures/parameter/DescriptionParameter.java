package com.analysis.structures.parameter;

/**
 * Class used to represent parameters in the code generator
 */
public class DescriptionParameter {
    private String name;
    private String type;
    private String value;

    public DescriptionParameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public DescriptionParameter(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DescriptionParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
