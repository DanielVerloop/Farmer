package com.analysis.util;

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
        String paramString="";
        for (int i = 0; i < params.size(); i++) {
            paramString += params.get(i) + ", ";
        }
        return paramString.substring(0, paramString.length() - 2);
    }
}
