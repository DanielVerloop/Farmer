package com.analysis;

import com.analysis.structures.Scenario;

import java.io.FileNotFoundException;
import java.util.List;

public interface Matcher {

    List<Scenario> getMatch();

    List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) throws FileNotFoundException;
}
