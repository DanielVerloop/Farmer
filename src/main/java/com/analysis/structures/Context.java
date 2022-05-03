package com.analysis.structures;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private String mainClass;
    private String subClass;
    private List<Rule> matchingRules = new ArrayList<>();

    public Context() {
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }


    public String getSubClass() {
        return subClass;
    }

    public void setSubClass(String subClass) {
        this.subClass = subClass;
    }

    public List<Rule> getMatchingRules() {
        return matchingRules;
    }

    public void setMatchingRules(List<Rule> matchingRules) {
        this.matchingRules = matchingRules;
    }

    public void addMatchingRule(Rule matchingRule) {
        this.matchingRules.add(matchingRule);
    }
}
