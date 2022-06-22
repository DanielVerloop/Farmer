package com.analysis.util.distance;

import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.DISCO;
import de.linguatools.disco.WrongWordspaceTypeException;
import info.debatty.java.stringsimilarity.Cosine;

import java.io.IOException;

public class DiscoStringSimilarity {
    public DISCO disco;

    public DiscoStringSimilarity() throws IOException, CorruptConfigFileException {
        this.disco = DISCO.load("src/main/resources/enwiki-20130403-word2vec-lm-mwl-lc-sim");
    }

    public double distance(String s1, String s2) throws IOException, WrongWordspaceTypeException {
        float distance = disco.secondOrderSimilarity(s1, s2, DISCO.getVectorSimilarity(DISCO.SimilarityMeasure.COSINE));
        return 1 - distance;
    }

    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        DiscoStringSimilarity model = new DiscoStringSimilarity();
        Cosine sim = new Cosine();
        System.out.println(model.distance("insert", "input amount"));
//        System.out.println(model.distance("inserts", "set amount"));
//        System.out.println(model.distance("insert", "setAmount"));
//        double result = model.distance("insert", "set product");
//        System.out.println(result);
        System.out.println("-----------------------");
        System.out.println(sim.distance("insert", "input"));
        System.out.println(new CosineSimilarity("insert", "input").getResult());
        System.out.println(sim.distance("find", "search"));
        System.out.println(new CosineSimilarity("find", "search").getResult());
        System.out.println(sim.distance("remove", "withdraw"));
        System.out.println(new CosineSimilarity("remove", "withdraw").getResult());
        System.out.println(new CosineSimilarity("inserts money dollars", "insert dollar").getResult());
        System.out.println(sim.distance("inserts money dollars", "check for change"));
    }
}
