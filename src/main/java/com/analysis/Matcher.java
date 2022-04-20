package com.analysis;

import com.analysis.structures.Scenario;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.WrongWordspaceTypeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface Matcher {

    List<Scenario> getMatch();

    List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException;
}
