package com.analysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ParameterParser {
    private HashMap<String, List<String>> types;
    private File inputFile;
    private Scanner input;

    public ParameterParser(File featureFile) throws FileNotFoundException {
        this.inputFile = featureFile;
        this.types = findParameterTypes();
    }

    public HashMap<String, List<String>> findParameterTypes() throws FileNotFoundException {
        HashMap<String, List<String>> result = new HashMap<>();
        input = new Scanner(inputFile);

        while (input.hasNext()) {
            String nextLine = input.nextLine().trim();

            //find the table
            if (nextLine.startsWith("Examples:")) {
                String[] headers = input.nextLine().trim()
                        .replaceFirst("^\\|\\s*", "")
                        .split("\\s*\\|\\s*");
                String[] variables = input.nextLine().trim()
                        .replaceFirst("^\\|\\s*", "")
                        .split("\\s*\\|\\s*");

                //add to map
                for (int i = 0; i < headers.length; i++) {
                    if (isInteger(variables[i])) {
                        result.computeIfAbsent("int", k -> new ArrayList<>()).add(headers[i]);
                    } else if (isDouble(variables[i])) {
                        result.computeIfAbsent("double", k -> new ArrayList<>()).add(headers[i]);
                    } else {
                        result.computeIfAbsent("String", k -> new ArrayList<>()).add(headers[i]);
                    }
                }
            }
        }


        return result;
    }

    public String getParameterType(String var) {
        String result = null;
        for (Map.Entry<String, List<String>> entry : types.entrySet()) {
            String type = entry.getKey();
            List<String> params = entry.getValue();
            for (String param : params) {
                if (param.equals(var)) {
                    result = type;
                    break;
                }
            }
        }
        return result;
    }

    private boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    public static void main(String[] args) throws FileNotFoundException {
//        ParameterParser parser = new ParameterParser();
//        System.out.println(parser.getTypes());
//
//    }

    public HashMap<String, List<String>> getTypes() {
        return types;
    }
}
