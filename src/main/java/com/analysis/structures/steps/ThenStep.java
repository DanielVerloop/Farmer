package com.analysis.structures.steps;

import com.analysis.util.SRLAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThenStep extends Step {
    private Set<String> verbs;
    private Map<String, String> advice;

    public ThenStep(String description, Map<String, List<String>> posResult, Map<String, List<String>> srlSentence) {
        super(description, posResult);
        this.verbs = srlSentence.keySet();
        this.advice = new SRLAnalyzer(srlSentence).generateAdvice();
    }

    public Set<String> getVerbs() {
        return verbs;
    }

    public String getAdvice(String key) {
        return advice.get(key);
    }

    @Override
    public void setMatchResult() {
        System.out.println("something unique!");
    }
}
