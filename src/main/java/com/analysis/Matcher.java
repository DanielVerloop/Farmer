package com.analysis;

import com.analysis.structures.Scenario;

import java.util.List;

public interface Matcher {

    List<Scenario> getMatch();

    List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis);
}
