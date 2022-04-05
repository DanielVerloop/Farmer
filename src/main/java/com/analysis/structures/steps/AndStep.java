package com.analysis.structures.steps;

import com.analysis.structures.Visitor;
import com.analysis.util.SRLAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gherkin And step
 * TODO:implement correct field getters
 */
public class AndStep extends Step {
    private Set<String> verbs;
    private Map<String, String> advice;//might be null

    public AndStep(String description, Map<String, List<String>> posResult, Map<String, List<String>> srlSentence) {
        super(description, posResult);
        this.verbs = srlSentence.keySet();
        this.advice = new SRLAnalyzer(srlSentence).generateAdvice();
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }
}
