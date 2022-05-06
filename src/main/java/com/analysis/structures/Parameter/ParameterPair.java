package com.analysis.structures.Parameter;

import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.util.List;

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
    public String toString() {
        return "ParameterPair{" +
                "baseParams=" + baseParams +
                ", originalParams=" + originalParams +
                '}';
    }
}
