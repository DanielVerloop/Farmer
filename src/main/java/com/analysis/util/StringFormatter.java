package com.analysis.util;

import com.analysis.structures.Parameter.DescriptionParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * helper class to camelCase strings
 */
public class StringFormatter {

    public String capitalizeWord(String str){
        String words[]=str.split("\\s");
        String capitalizeWord="";
        for(String w:words){
            String first=w.substring(0,1);
            String afterfirst=w.substring(1);
            capitalizeWord+=first.toUpperCase()+afterfirst+" ";
        }
        return capitalizeWord.trim();
    }

    public String camelCase(String str) {
        String words[]=str.split("\\s");
        String capitalizeWord="";
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                capitalizeWord += words[i].toLowerCase();
            } else {
                String w = words[i];
                String first = w.substring(0, 1);
                String afterfirst = w.substring(1);
                capitalizeWord += first.toUpperCase() + afterfirst + " ";
            }
        }
        return capitalizeWord.trim().replaceAll("\\s","");
    }

    public String parseParameters(List<String> params) {
        if (params == null) {
            return null;
        }
        if (params.size() == 0) {
            return null;
        }
        String paramString="";
        for (int i = 0; i < params.size(); i++) {
            paramString += params.get(i) + ", ";
        }
        return paramString.substring(0, paramString.length() - 2);
    }

    public String splitMethodName(String name) {
        String[] split = name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        String result = String.join(" ", split);
        return result.toLowerCase();
    }

    public List<String> orderParameters(String description, List<DescriptionParameter> parameters) {
        List<DescriptionParameter> temp = new ArrayList<>(parameters);
        List<String> result = new ArrayList<>();
        String[] descr = description.split("\\s");
        List<String> paramNames = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            paramNames.add(parameters.get(i).getName());
        }
        for (int i = 0; i < descr.length; i++) {
            for (int j = 0; j < temp.size(); j++) {
                if (temp.get(j).getValue() != null) {//number
                    if (descr[i].equals(temp.get(j).getValue())) {
                        result.add(temp.get(j).getType() + " " + temp.get(j).getName());
                        temp.remove(j);
                    }
                } else {//other types
                    if (descr[i].equals(temp.get(j).getName())) {
                        result.add(temp.get(j).getType() + " " + temp.get(j).getName());
                        temp.remove(j);
                    }
                }
            }
        }
        return result;
    }
}
