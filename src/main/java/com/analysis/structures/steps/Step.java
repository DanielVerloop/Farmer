package com.analysis.structures.steps;

import com.analysis.structures.Rule;
import com.analysis.structures.Scenario;

import java.util.List;
import java.util.Map;

/**
 * base Step interface
 */
public abstract class Step {
    private final String description;
    private final List<String> nouns;
    private final List<String> numbers;
    private final List<String> parameters;
    private final Scenario parent;
    private Rule matchResult;

    public Step(String description, Map<String, List<String>> posResult, Scenario parent) {
        this.description = description;
        this.nouns = posResult.get("nouns");
        this.numbers = posResult.get("numbers");
        this.parameters = posResult.get("parameters");
        this.parent = parent;
    }

    //All class getters
    public String getDescription() {
        return description;
    }

    public List<String> getNouns() {
        return nouns;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Method to set match result of each specific step
     * @param match
     */
    public void setMatchResult(Rule match) {
        this.matchResult = match;
    }

    /**
     * Method get match result
     * @return
     */
    public Rule getMatchResult() {
        return matchResult;
    }

    /**
     * toString() for pretty printing
     * @return Stringyfied object
     */
    @Override
    public String toString() {
        return "Step{" +
                "description='" + description + '\'' +
                ", nouns=" + nouns +
                ", numbers=" + numbers +
                ", parameters=" + parameters +
                '}';
    }

    public Scenario getParent() {
        return parent;
    }
}
