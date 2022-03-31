package com.analysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

/**
 * Helper class for parsing JSON NLP-file
 */
public class NLPFileReader {
    JSONObject jsonFile;

    public NLPFileReader(String filepath) {
        Object parsedJSONFile = null;
        try {
            parsedJSONFile = new JSONParser().parse(new FileReader(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        jsonFile = (JSONObject) parsedJSONFile;
    }

    public List<String> getFilenames() {
        ArrayList<String> filenames = new ArrayList<String>();
        JSONArray allFiles = (JSONArray) this.jsonFile.get("files");

        for (Object allFile : allFiles) {
            JSONObject file = (JSONObject) allFile;
            filenames.add((String) file.get("name"));
        }
        return filenames;
    }

    /**
     * returns all step descriptions
     * @param fileName
     * @return
     */
    public List<String> getSteps(String fileName) {
        JSONObject stepFile = getFileFromJson(fileName);

        // iterate over the Given, When, Then and extract the step descriptions
        List<String> descriptions = new ArrayList<String>();
        for (Object givenStep : (JSONArray) stepFile.get("given")) descriptions.add((String) ((JSONObject) givenStep).get("description"));
        for (Object whenStep : (JSONArray) stepFile.get("when")) descriptions.add((String) ((JSONObject) whenStep).get("description"));
        for (Object thenStep : (JSONArray) stepFile.get("then")) descriptions.add((String) ((JSONObject) thenStep).get("description"));

        return descriptions;
    }

    private JSONObject getFileFromJson(String fileName) {
        JSONArray allFiles = (JSONArray) this.jsonFile.get("files");

        JSONObject stepFile = null;
        // Get the JSONObject of the correct file
        for (Object allFile : allFiles) {
            JSONObject file = (JSONObject) allFile;
            if (file.get("name").equals(fileName)) {
                stepFile = (JSONObject) file.get("scenarios");
                break;
            }
        }
        return stepFile;
    }

    /**
     * Get NLP analysis of a specific step
     *
     * @param fileName name of file step belongs to
     * @param stepDesc description of step
     * @returns NLP analysis
     */
    public Object getAnalysis(String fileName, String stepDesc) {
        JSONObject stepFile = getFileFromJson(fileName);

        if (stepDesc.startsWith("Given")) {
            for (Object givenStep : (JSONArray) stepFile.get("given")) {
                if ((((JSONObject) givenStep).get("description")).equals(stepDesc))
                    return ((JSONObject) givenStep).get("analysis");
            }
        }
        if (stepDesc.startsWith("When")) {
            for (Object whenStep : (JSONArray) stepFile.get("when")) {
                if ((((JSONObject) whenStep).get("description")).equals(stepDesc))
                    return ((JSONObject) whenStep).get("analysis");
            }
        }
        if (stepDesc.startsWith("Then")) {
            for (Object thenStep : (JSONArray) stepFile.get("then")) {
                if ((((JSONObject) thenStep).get("description")).equals(stepDesc))
                    return ((JSONObject) thenStep).get("analysis");
            }
        }
        return new IllegalStateException("Incorrect step description!"); //Should never be able to happen
    }

    public Map<String, List<String>> getPos(String fileName, String stepDesc) {
        JSONObject stepFile = getFileFromJson(fileName);
        HashMap<String, List<String>> result = new HashMap<>();
        JSONObject posInfo = new JSONObject();

        switch (stepDesc.toLowerCase().substring(0, 4)) {
            case "give":
                for (Object givenStep : (JSONArray) stepFile.get("given")) {
                    if ((((JSONObject) givenStep).get("description")).equals(stepDesc)) {
                        // triple cast to let the compiler know the correct types
                        posInfo = (JSONObject) ((JSONArray) ((JSONObject) givenStep).get("analysis")).get(0);
                    }
                }
                break;
            case "when":
                for (Object whenStep : (JSONArray) stepFile.get("when")) {
                    if ((((JSONObject) whenStep).get("description")).equals(stepDesc)) {
                        // triple cast to let the compiler know the correct types
                        posInfo = (JSONObject) ((JSONArray) ((JSONObject) whenStep).get("analysis")).get(0);
                    }
                }
                break;
            case "then":
                for (Object thenStep : (JSONArray) stepFile.get("then")) {
                    if ((((JSONObject) thenStep).get("description")).equals(stepDesc)) {
                        // triple cast to let the compiler know the correct types
                        posInfo = (JSONObject) ((JSONArray) ((JSONObject) thenStep).get("analysis")).get(0);
                    }
                }
                break;
            default:
                throw new RuntimeException("Unknown step description");
        }

        //add nouns to return data structure
        JSONArray tempNouns = (JSONArray) posInfo.get("nouns");
        List<String> nouns = new ArrayList<>();
        for (int i = 0; i < tempNouns.size(); i++) {
            nouns.add(tempNouns.get(i).toString());
        }
        result.put("nouns", nouns);

        //add numbers to return data structure
        JSONArray cardinals = (JSONArray) posInfo.get("numbers");
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < cardinals.size(); i++) {
            numbers.add(cardinals.get(i).toString());
        }
        result.put("numbers", numbers);
        return result;
    }

    public Map<String, List<String>> getSrl(String fileName, String stepDesc) {
        JSONObject stepFile = getFileFromJson(fileName);
        HashMap<String, List<String>> result = new HashMap<>();
        JSONArray srlInfo = new JSONArray();

        switch (stepDesc.toLowerCase().substring(0, 4)) {
            case "when":
                for (Object whenStep : (JSONArray) stepFile.get("when")) {
                    if ((((JSONObject) whenStep).get("description")).equals(stepDesc)) {
                        // triple cast to let the compiler know the correct types
                        srlInfo = (JSONArray) ((JSONArray) ((JSONObject) whenStep).get("analysis")).get(1);
                    }
                }
                break;
            case "then":
                for (Object thenStep : (JSONArray) stepFile.get("then")) {
                    if ((((JSONObject) thenStep).get("description")).equals(stepDesc)) {
                        // triple cast to let the compiler know the correct types
                        srlInfo = (JSONArray) ((JSONArray) ((JSONObject) thenStep).get("analysis")).get(1);
                    }
                }
                break;
            default:
                throw new RuntimeException("Unknown step description");
        }

        for (Object srl : srlInfo) {
            String verb = (String) ((JSONArray) srl).get(0);
            List<String> labels = new ArrayList<>();
            ((JSONArray) ((JSONArray) srl).get(1)).stream().forEach(s -> {
                labels.add(s.toString());
            });
            result.put(verb, labels);
        }
        
        return result;
    }
}
