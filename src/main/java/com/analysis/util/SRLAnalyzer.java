package com.analysis.util;

import java.util.*;

public class SRLAnalyzer {
    private Map<String, List<String>> srlLabels;
    private Set<String> verbs;

    public SRLAnalyzer(Map<String, List<String>> srlSentence) {
        this.verbs = srlSentence.keySet();
        this.srlLabels = srlSentence;
    }

    /**
     * Extract needed info from SRL data
     * @return mapped advice
     */
    public Map<String, String> generateAdvice() {
        if (srlLabels == null || srlLabels.isEmpty()) {
            return null;
        }
        //TODO implement for all verbs
        //TODO returnNumberType if sanitation is needed on values (remove useless words: the, a, an, I, we, etc...)
        Map<String, String> advice = new HashMap<>();
        ArrayList<String> labels = getLabels(verbs.iterator().next());
        for (String label : labels) {
            if (!label.contains(":")) continue; //TODO: this means the NLP was faulty, for now ignore
            String role = label.split(":")[0].trim();
            String value = label.split(":")[1].trim();

            //find all important roles and map them
            switch (role) {
                case "V":
                    advice.put("V", value);
                    break;
                case "ARG0":
                    advice.put("ARG0", value);
                    break;
                case "ARG1":
                    advice.put("ARG1", value);
                    break;
                case "ARG2":
                    advice.put("ARG2", value);
                    break;
                default: // ignore all other cases
                    break;
            }
        }
        return advice;
    }

    private ArrayList<String> getLabels(String key) {
        List<String> labels = srlLabels.get(key);
        return (ArrayList<String>) labels;
    }
}
