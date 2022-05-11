package com.analysis.structures.parameter;

import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ParameterTree {
    private List<ParameterTreeNode> nodes;

    public ParameterTree() {
        this.nodes = new ArrayList<>();
    }

    public void addNode(ParameterTreeNode node) {
        this.nodes.add(node);
    }

    public List<ParameterPair> createPairList(List<Parameter> parameters) {
        List<List<Parameter>> combinations = new ArrayList<>();
        for (ParameterTreeNode node: nodes) {
            for (List<Parameter> param : node.getParameterList()) {
                combinations.add(param);
            }
        }

        List<List<List<Parameter>>> combinationList = getAllCombinations(combinations, nodes.size());
        List<List<List<Parameter>>> validCombinations = new ArrayList<>();
        for (List<List<Parameter>> combination : combinationList) {
            boolean correct = true;
            for (int i = 0; i < nodes.size(); i++) {
                if (!(nodes.get(i).getParameterList().contains(combination.get(i)))) {
                    correct = false;
                }
            }
            if (correct) validCombinations.add(combination);
        }

        //reduce to List of List Parameters
        List<List<Parameter>> result = new ArrayList<>();
        validCombinations.forEach(q -> {
            List<Parameter> temp = new ArrayList<>();
            q.forEach(p -> p.forEach(parameter -> {
                temp.add(parameter);
            }));
            result.add(temp);
        });

        //create the pairs
        List<ParameterPair> pairs = new ArrayList<>();
        for (List<Parameter> parameterList: result) {
            pairs.add(new ParameterPair(parameterList, parameters));
        }
        return pairs;
    }


    private static <T> List<List<T>> getAllCombinations(List<T> values, int size) {

        if (0 == size) {
            return Collections.singletonList(Collections.<T> emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = getAllCombinations(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(getAllCombinations(subSet, size));

        return combination;
    }
}
