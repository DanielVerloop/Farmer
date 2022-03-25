package com.analysis;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
}
