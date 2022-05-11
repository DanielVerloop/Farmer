package com.analysis.structures.parameter;

import java.util.Objects;

public class Method {
    private String name;
    private ParameterPair pair;

    public Method(String name, ParameterPair parameterPair) {
        this.name = name;
        this.pair = parameterPair;
    }

    public String getName() {
        return name;
    }

    public ParameterPair getPair() {
        return pair;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Method)) return false;
        Method method = (Method) o;
        return getName().equals(method.getName()) && Objects.equals(getPair(), method.getPair());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPair());
    }

    @Override
    public String toString() {
        return "Method{" +
                "name='" + name + '\'' +
                ", pair=" + pair +
                '}';
    }
}
