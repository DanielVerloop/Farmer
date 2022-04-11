package com.analysis.structures;


import com.analysis.structures.steps.Step;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a Gherkin scenario analysis
 */
public class Scenario {
    private List<Step> steps = new ArrayList<>();

    public Scenario() {

    }

    public Scenario(List<Step> steps) {
        this.steps = steps;
    }

    public void addStep(Step step) {
        this.steps.add(step);
    }

    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "steps=" + steps +
                '}';
    }
}
