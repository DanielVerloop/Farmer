package com.analysis;

import com.analysis.structures.Scenario;
import com.analysis.structures.steps.*;
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
    String featureFile;

    public NLPFileReader(String filepath, String featurePath) {
        this.featureFile = featurePath;
        Object parsedJSONFile = null;
        try {
            parsedJSONFile = new JSONParser().parse(new FileReader(filepath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        jsonFile = (JSONObject) parsedJSONFile;
    }

    /**
     * Processes JSON of a feature file
     * @param fileName name of feature file
     * @return a list of Scenarios
     */
    public List<Scenario> getScenarios(String fileName) throws FileNotFoundException {
        JSONArray scenariosFromFile = getScenariosFromFile(fileName);
        List<Scenario> scenarios = new ArrayList<>();

        for (Object o : scenariosFromFile) {
            JSONObject scenario = (JSONObject) o;
            Scenario s = new Scenario(this.featureFile);

            //Get the pos-tagger results
            Map<String, List<String>> posResultGiven = this.getPos((JSONObject) scenario.get("given"));
            Map<String, List<String>> posResultWhen = this.getPos((JSONObject) scenario.get("when"));
            Map<String, List<String>> posResultThen = this.getPos((JSONObject) scenario.get("then"));
            //Get the SRL result
            Map<String, List<String>> srlWhen = this.getSRL((JSONObject) scenario.get("when"));
            Map<String, List<String>> srlThen = this.getSRL((JSONObject) scenario.get("then"));

            //Handle all and steps
            List<Step> gAnd = new ArrayList<>();
            List<Step> wAnd = new ArrayList<>();
            List<Step> tAnd = new ArrayList<>();
            if (((JSONArray) scenario.get("gAnd")).size() != 0) {
                gAnd = handleAndSteps(scenario, s, "gAnd");
            }
            if (((JSONArray) scenario.get("wAnd")).size() != 0) {
                wAnd = handleAndSteps(scenario, s, "wAnd");
            }
            if (((JSONArray) scenario.get("tAnd")).size() != 0) {
                tAnd = handleAndSteps(scenario, s, "tAnd");
            }

            //Add every step type to scenario
            GivenStep givenStep = new GivenStep(
                    (String) ((JSONObject) scenario.get("given")).get("description"), posResultGiven, s, gAnd);
            WhenStep whenStep = new WhenStep(
                    (String) ((JSONObject) scenario.get("when")).get("description"), posResultWhen, srlWhen, s, wAnd);
            ThenStep thenStep = new ThenStep(
                    (String) ((JSONObject) scenario.get("then")).get("description"), posResultThen, srlThen, s, tAnd);
            //Add created steps to scenario
            s.addStep(givenStep);
            s.addStep(whenStep);
            s.addStep(thenStep);

            scenarios.add(s);
        }

        return scenarios;
    }

    private List<Step> handleAndSteps(JSONObject scenario, Scenario s, String type) {
        JSONArray steps = (JSONArray) scenario.get(type);
        List<Step> result = new ArrayList<>();
        for (Object ob: steps) {
            JSONObject step = (JSONObject) ob;
            Step andStep;
            switch (type) {
                case "gAnd":
                    andStep = new AndStep(
                            (String) step.get("description"),
                            getPos(step),
                            getSRL(step),
                            s
                    );
                    result.add(andStep);
                    break;
                case "wAnd":
                    andStep = new WhenStep(
                            (String) step.get("description"),
                            getPos(step),
                            getSRL(step),
                            s,
                            null
                    );
                    result.add(andStep);
                    break;
                case "tAnd":
                    andStep = new ThenStep(
                            (String) step.get("description"),
                            getPos(step),
                            getSRL(step),
                            s,
                            null
                    );
                    result.add(andStep);
                    break;
                default:
                    break;
            }
        }
        return result;
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
    private Map<String, List<String>> getPos(JSONObject step) {
        JSONObject pos = (JSONObject) ((JSONArray) step.get("analysis")).get(0);//Get the pos result

        Map<String, List<String>> result = new HashMap<>();
        //add to map
        result.put("numbers", transform2List((JSONArray) pos.get("numbers")));
        result.put("nouns", transform2List((JSONArray) pos.get("nouns")));
        result.put("parameters", transform2List((JSONArray) pos.get("parameters")));

        return result;
    }

    private List<String> transform2List(JSONArray arr) {
        if (arr == null) return null;
        List<String> result = new ArrayList<>();
        for (Object o : arr) {
            result.add(o.toString());
        }
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

    //for testing only!!
    public static void main(String[] args) throws FileNotFoundException {
        List<Scenario> result = new NLPFileReader("src/main/resources/nlp_results.json", "src/returnNumberType/resources/features/vendingMachine.feature")
                .getScenarios("vendingMachine.feature");
        System.out.println(result);
    }
}
