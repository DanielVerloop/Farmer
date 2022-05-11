package com.analysis.structures.parameter;

import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParameterPair {
    private List<Parameter> baseParams = new ArrayList<>();
    private List<Parameter> originalParams = new ArrayList<>();

    public ParameterPair(List<Parameter> base, List<Parameter> original) {
        this.baseParams = base;
        this.originalParams = original;
    }

    public List<Parameter> getBaseParams() {
        return baseParams;
    }

    public List<Parameter> getOriginalParams() {
        return originalParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterPair)) return false;
        ParameterPair that = (ParameterPair) o;
        return getBaseParams().equals(that.getBaseParams()) && getOriginalParams().equals(that.getOriginalParams());
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseParams, originalParams);
    }

    @Override
    public String toString() {
        return "ParameterPair{" +
                "baseParams=" + baseParams +
                ", originalParams=" + originalParams +
                '}';
    }
}
