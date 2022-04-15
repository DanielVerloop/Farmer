package com.analysis.structures.steps;

import com.analysis.structures.Scenario;
import com.analysis.util.SRLAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThenStep extends Step {
    private Set<String> verbs;
    private Map<String, String> advice;

    public ThenStep(String description, Map<String, List<String>> posResult,
                    Map<String, List<String>> srlSentence, Scenario parent, List<Step> andSteps) {
        super(description, posResult, parent, andSteps);
        this.verbs = srlSentence.keySet();
        this.advice = new SRLAnalyzer(srlSentence).generateAdvice();
    }

    public Set<String> getVerbs() {
        return verbs;
    }

    public Map<String, String> getAdvice() {
        return this.advice;
    }

    public String getAdvice(String key) {
        return advice.get(key);
    }


    @Override
    public String toString() {
        return "ThenStep{" +
                "description='" + getDescription() + '\'' +
                ", nouns=" + getNouns() +
                ", numbers=" + getNumbers() +
                ", parameters=" + getParameters() +
                ", verbs=" + verbs +
                ", advice=" + advice +
                '}';
    }
}
