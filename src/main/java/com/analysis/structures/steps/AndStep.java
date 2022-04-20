package com.analysis.structures.steps;

import com.analysis.structures.Scenario;
import com.analysis.util.SRLAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gherkin And step
 * Extends GivenStep
 */
public class AndStep extends GivenStep {
    private Step linkedStep;// step to keep track of (Given,When, Then)
    private Set<String> verbs;
    private Map<String, String> advice;//might be null

    public Set<String> getVerbs() {
        return verbs;
    }

    public Map<String, String> getAdvice() {
        return advice;
    }

    public Step getLinkedStep() {
        return linkedStep;
    }

    public AndStep(String description, Map<String, List<String>> posResult, Map<String,
            List<String>> srlSentence, Scenario parent) {
        super(description, posResult, parent, null);
        this.verbs = srlSentence.keySet();
        this.advice = new SRLAnalyzer(srlSentence).generateAdvice();
        this.linkedStep = linkedStep;
    }

    @Override
    public String toString() {
        return "AndStep{" +
                "description='" + getDescription() + '\'' +
                ", nouns=" + getNouns() +
                ", numbers=" + getNumbers() +
                ", parameters=" + getParameters() +
                ", verbs=" + verbs +
                ", advice=" + advice +
                '}';
    }
}
