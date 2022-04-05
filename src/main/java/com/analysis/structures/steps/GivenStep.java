package com.analysis.structures.steps;

import com.analysis.structures.Visitor;

import java.util.List;
import java.util.Map;

public class GivenStep extends Step {

    public GivenStep(String description, Map<String, List<String>> posResult) {
        super(description, posResult);
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
