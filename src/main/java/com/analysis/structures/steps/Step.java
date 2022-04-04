package com.analysis.structures.steps;

import java.util.List;
import java.util.Map;

/**
 * base interface
 */
public abstract class Step {
    private final String description;
    private final List<String> nouns;
    private final List<String> numbers;
    private final List<String> parameters;

    public Step(String description, Map<String, List<String>> posResult) {
        this.description = description;
        this.nouns = posResult.get("nouns");
        this.numbers = posResult.get("numbers");
        this.parameters = posResult.get("parameters");
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
     */
    public abstract void setMatchResult();
}
