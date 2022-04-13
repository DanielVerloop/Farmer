package com.analysis.structures;


import com.analysis.structures.steps.Step;
import com.analysis.util.ParameterParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a Gherkin scenario analysis
 */
public class Scenario {
    private List<Step> steps = new ArrayList<>();
    private ParameterParser typeSolver;

    public Scenario() {

    }

    public Scenario(String featureFile) throws FileNotFoundException {
        this.typeSolver = new ParameterParser(new File(featureFile));
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

    public ParameterParser getTypeSolver() {
        return typeSolver;
    }
}
