package com.analysis.structures;

import com.analysis.structures.steps.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchHandler implements Visitor{
    //context to keep all match info of previous steps during the matching process
    private Map<Step, List<String>> context = new HashMap<>();

    @Override
    public void visit(GivenStep givenStep) {

    }

    @Override
    public void visit(WhenStep whenStep) {

    }

    @Override
    public void visit(ThenStep thenStep) {

    }

    @Override
    public void visit(AndStep andStep) {

    }

    @Override
    public void visit(Scenario scenario) {
        //update context per step


    }

    @Override
    public void visit(Step step) {

    }

    public void add2Context(Step step, List<String> result) {
        this.context.put(step, result);
    }

    public void setContext(Map<Step, List<String>> context) {
        this.context = context;
    }

    public Map<Step, List<String>> getContext() {
        return context;
    }
}
