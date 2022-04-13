package com.analysis;

import com.analysis.structures.Scenario;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class CosineSimilarityMatcher implements Matcher{
    private List<Scenario> match;

    @Override
    public List<Scenario> getMatch() {
        return match;
    }

    public CosineSimilarityMatcher(File targetDir, List<Scenario> scenarios) throws FileNotFoundException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        match = this.match(scenarios, analysis);
     }

    @Override
    public List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) {
        return null;
    }
}
