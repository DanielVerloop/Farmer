package com.analysis.structures;

import com.analysis.structures.steps.*;

public interface Visitor {

    void visit(GivenStep givenStep);
    void visit(WhenStep whenStep);
    void visit(ThenStep thenStep);
    void visit(AndStep andStep);
    void visit(Scenario scenario);
    void visit(Step step);
}
