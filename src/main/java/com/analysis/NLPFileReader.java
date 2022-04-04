package com.analysis;

import com.analysis.structures.Scenario;
import com.analysis.structures.steps.GivenStep;
import com.analysis.structures.steps.Step;
import com.analysis.structures.steps.WhenStep;
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
     * Processes JSON of a feature file
     * @param fileName name of feature file
     * @return a list of Scenarios
     */
    public List<Scenario> getScenarios(String fileName) {
        JSONArray scenariosFromFile = getScenariosFromFile(fileName);
        List<Scenario> scenarios = new ArrayList<>();

        for (Object o : scenariosFromFile) {
            JSONObject scenario = (JSONObject) o;
            Scenario s = new Scenario();

            //Get the pos-tagger results
            Map<String, List<String>> posResultGiven = this.getPos((JSONObject) scenario.get("given"));
            Map<String, List<String>> posResultWhen = this.getPos((JSONObject) scenario.get("when"));
            Map<String, List<String>> posResultThen = this.getPos((JSONObject) scenario.get("then"));
            //Get the SRL result
            Map<String, List<String>> srlWhen = this.getSRL((JSONObject) scenario.get("when"));
            Map<String, List<String>> srlThen = this.getSRL((JSONObject) scenario.get("then"));

            //Add every step type to scenario
            GivenStep givenStep = new GivenStep(
                    (String) ((JSONObject) scenario.get("given")).get("description"), posResultGiven);
            WhenStep whenStep = new WhenStep(
                    (String) ((JSONObject) scenario.get("when")).get("description"), posResultWhen, srlWhen);
            WhenStep thenStep = new WhenStep(
                    (String) ((JSONObject) scenario.get("then")).get("description"), posResultThen, srlThen);
            //TODO: handle AND steps
            //Add created steps to scenario
            s.setSteps(Arrays.asList(givenStep, whenStep, thenStep));
            scenarios.add(s);
        }

        return scenarios;
    }

    private JSONArray getScenariosFromFile(String fileName) {
        JSONArray allFiles = (JSONArray) this.jsonFile.get("files");

        JSONArray stepFile = null;
        // Get the JSONObject of the correct file
        for (Object allFile : allFiles) {
            JSONObject file = (JSONObject) allFile;
            if (file.get("name").equals(fileName)) {
                stepFile = (JSONArray) file.get("scenarios");
                break;
            }
        }
        return stepFile;
    }

    /**
     * Get pos-tagger result from given, when and then steps
     * @param step JSONObject of Step of type given, when, then
     * @return map of the result
     */
    public Map<String, List<String>> getPos(JSONObject step) {
        String stepDesc = (String) step.get("description");
        Map<String, List<String>> result = new HashMap<>();

        //Get the pos result
        JSONObject pos = (JSONObject) ((JSONArray) step.get("analysis")).get(0);
        //add to map
        result.put("numbers", (List<String>) pos.get("numbers"));
        result.put("nouns", (List<String>) pos.get("nouns"));
        result.put("parameters", (List<String>) pos.get("parameters"));

        return result;
    }

    private Map<String, List<String>> getSRL(JSONObject step) {
        Map<String, List<String>> result = new HashMap<>();
        JSONArray srlInfo = (JSONArray) ((JSONArray) step.get("analysis")).get(1);

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

    public static void main(String[] args) {
        NLPFileReader jsonResult = new NLPFileReader("src/main/resources/nlp_results_new.json");
        String name = jsonResult.getFilenames().get(0);
//        jsonResult.getScenariosFromFile(name);
        jsonResult.getScenarios(name);
    }
}
