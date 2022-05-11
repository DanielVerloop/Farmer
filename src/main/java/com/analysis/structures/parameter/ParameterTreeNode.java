package com.analysis.structures.parameter;

import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.util.List;

public class ParameterTreeNode {
    List<List<Parameter>> parameterList;

    public ParameterTreeNode() {
        this.parameterList = new ArrayList<>();
    }

    public List<List<Parameter>> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<List<Parameter>> parameterList) {
        this.parameterList = parameterList;
    }

    public void addParameterList(List<Parameter> parameterList) {
        this.parameterList.add(parameterList);
    }

    @Override
    public String toString() {
        return "ParameterTreeNode{" +
                "parameterList=" + parameterList +
                '}';
    }
}
