package com.analysis.structures.constructor;

import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.util.List;

public class ConstructorPair {
    private List<Parameter> baseParams = new ArrayList<>();
    private List<Parameter> originalParams = new ArrayList<>();

    public ConstructorPair(List<Parameter> base, List<Parameter> original) {
        this.baseParams = base;
        this.originalParams = original;
    }

    public List<Parameter> getBaseParams() {
        return baseParams;
    }

    public List<Parameter> getOriginalParams() {
        return originalParams;
    }
}
