package com.analysis.util.distance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CosineSimilarity {
    private double result;

    public CosineSimilarity(String s1, String s2) {
        this.result = similarScoreCosWord(s1, s2);
    }

    /**
     * text1, text2 are the segmented character strings, cosine similarity matching degree
     * @param text1 The segmented string eg: This leather boot has a big number
     * @param text2 The segmented string eg: the number of this leather boot is too small
     * @return
     */
    private double similarScoreCosWord(String text1, String text2){
        if(text1 == null || text2 == null)
            return 0.0;
        else if("".equals(text1)&&"".equals(text2)) return 1.0;
        Set<String> word = new TreeSet<>();
        String[] text1Array = text1.split(" ");
        String[] text2Array = text2.split(" ");
        Map<String, Integer> text1Map = new HashMap<>();
        Map<String, Integer> text2Map = new HashMap<>();
        for(int i=0;i<text1Array.length;i++){
            if(text1Map.get(text1Array[i])==null) text1Map.put(text1Array[i],1);
            else text1Map.put(text1Array[i],text1Map.get(text1Array[i])+1);
            word.add(text1Array[i]);
        }
        for(int j=0;j<text2Array.length;j++){
            if(text2Map.get(text2Array[j]) == null) text2Map.put(text2Array[j],1);
            else text2Map.put(text2Array[j],text2Map.get(text2Array[j])+1);
            word.add(text2Array[j]);
        }
        double xy = 0.0;
        double x = 0.0;
        double y = 0.0;
        //Calculate
        for (String it : word) {
            Integer t1 = text1Map.get(it)==null?0:text1Map.get(it);
            Integer t2 = text2Map.get(it)==null?0:text2Map.get(it);
            xy+=t1*t2;
            x+=Math.pow(t1, 2);
            y+=Math.pow(t2, 2);
        }
        if(x==0.0||y==0.0) return 0.0;
        return xy/Math.sqrt(x*y);
    }

    public double getResult() {
        return result;
    }
}
