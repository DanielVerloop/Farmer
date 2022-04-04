package com.analysis.structures.steps;

import java.util.List;
import java.util.Map;

public class GivenStep extends Step {

    public GivenStep(String description, Map<String, List<String>> posResult) {
        super(description, posResult);
    }

    @Override
    public void setMatchResult() {
        System.out.println("setMatchResult given step!");
    }
}
