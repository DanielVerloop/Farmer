package com.analysis.structures.steps;

import com.analysis.structures.Scenario;

import java.util.List;
import java.util.Map;

public class GivenStep extends Step {

    public GivenStep(String description, Map<String, List<String>> posResult, Scenario parent) {
        super(description, posResult, parent);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
