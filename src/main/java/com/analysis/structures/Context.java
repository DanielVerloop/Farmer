package com.analysis.structures;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private String mainClass;
    private String subClass;
    private List<String> InitialSetOfClasses;
    private List<List<Rule>> matchingRules = new ArrayList<>();

    public Context() {
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public List<String> getInitClasses() {
        return InitialSetOfClasses;
    }

    public void setInitClasses(List<String> initialSetOfClasses) {
        InitialSetOfClasses = initialSetOfClasses;
    }

    public String getSubClass() {
        return subClass;
    }

    public void setSubClass(String subClass) {
        this.subClass = subClass;
    }

    public List<List<Rule>> getMatchingRules() {
        return matchingRules;
    }

    public void addMatchingRule(List<Rule> matchingRule) {
        this.matchingRules.add(matchingRule);
    }
}
