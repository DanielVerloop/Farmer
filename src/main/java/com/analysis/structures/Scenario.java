package com.analysis.structures;


import com.analysis.structures.steps.Step;

import java.util.List;

/**
 * Class to represent a Gherkin scenario analysis
 */
public class Scenario {
    private List<Step> steps;

    public Scenario() {

    }

    public Scenario(List<Step> steps) {
        this.steps = steps;
    }

    public void addStep(Step step) {
        this.steps.add(step);
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void visit(Visitor visitor) {
        visitor.visit(this);
    }

}
