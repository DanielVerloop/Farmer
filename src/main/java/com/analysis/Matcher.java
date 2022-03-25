package com.analysis;

import com.analysis.util.LevenshteinDistance;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Text-to-Code Matcher
 * This class should be able to match functions to the step descriptions
 */
public class Matcher {

    /**
     * Matches all descriptions to code
     * @return TODO: classname or suggested methods
     */
    public Map<String, List<String>> match(HashMap<String, List<String>> codeAnalysis, NLPFileReader json, String stepFile) {
        String[] descriptions = json.getSteps(stepFile).toArray(new String[0]);
        List<String> result = new ArrayList<>();

        for (String description : descriptions) {
            JSONArray nlpAnalysis = (JSONArray) json.getAnalysis(stepFile, description);
//            System.out.println(nlpAnalysis);
            System.out.println(description);
            JSONArray posTagger = (JSONArray) nlpAnalysis.get(0);// Get pos-tagger result

            if (description.startsWith("Given")) {
                result.add(matchGiven(posTagger, codeAnalysis));
            } else if (description.startsWith("When")) {
                JSONArray srlSentence = (JSONArray) nlpAnalysis.get(1);
                result.add(matchWhen(posTagger, srlSentence, codeAnalysis));
                break;
            } else if (description.startsWith("Then")) {
                JSONArray srlSentence = (JSONArray) nlpAnalysis.get(1);
                result.add(matchThen(posTagger, srlSentence, codeAnalysis));
            } else {
                System.out.println(description);
            }
        }
        System.out.println(result);

        //TODO: return datastructure to generate code from
        // example =
        // {
        //   "description"={Advice="Object instantiation", Type="BankAccount", parameters={0}
        // }
        return null;
    }

    /**
     * Helper function to analyze a single given-step
     * @param posTagger pos-tagger result
     * @param codeAnalysis code analysis
     * @return info for generator to be able to generate code
     */
    private String matchGiven(JSONArray posTagger, HashMap<String, List<String>> codeAnalysis) {
//        HashMap<String, List<String>> result = new HashMap<>();
        List<List<String>> posTags = convertToList(posTagger);
        String[] nouns = extractNouns(posTags).toArray(new String[0]);
        List<String> numbers = extractNumbers(posTags);

        //for each class compute levenshtein distance
        HashMap<String, String> bestMatchedClass = new HashMap<>(); // {word=[suggested class]}
        for (String noun : nouns) {
            int smallestDist = Integer.MAX_VALUE;
            String matchedClass = "";
            for (Map.Entry<String, List<String>> entry : codeAnalysis.entrySet()) {
                String s = entry.getKey();
                int dist = new LevenshteinDistance(s, noun).getDistance();
                if (dist < smallestDist) {
                    smallestDist = dist;
                    matchedClass = s;
                }
            }
            bestMatchedClass.put(noun, matchedClass);
        }

        //Matched class
        String className = findMostCommon(bestMatchedClass);
        //TODO:add parameters for object instantiation

        return className;
    }

    private String matchWhen(JSONArray posTagger, JSONArray srlSentence, HashMap<String, List<String>> codeAnalysis) {
        //TODO: for each verb analyse using srl
        // use ARG0, ARG1 and ARG2 to see what method is supposed to do
        // then use this to suggest method calls
        List<List<String>> posTags = convertToList(posTagger);
        List<String> verbs = extractVerbs(posTags);
        String[] nouns = extractNouns(posTags).toArray(new String[0]);
        List<String> numbers = extractNumbers(posTags);

        System.out.println(verbs);

        return "";
    }

    private String matchThen(JSONArray posTagger, JSONArray srlSentence, HashMap<String, List<String>> codeAnalysis) {
        return "";
    }

    private String findMostCommon(HashMap<String, String> bestMatchedClass) {
        Map<String, Long> counter = bestMatchedClass
                .values()
                .stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        return counter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();
    }

    private List<String> extractVerbs(List<List<String>> posTagger) {
        List<String> verbs = new ArrayList<>();
        for (List<String> pair : posTagger) {
            if (pair.get(1).startsWith("VB")) // verb whatever tense
                verbs.add(pair.get(0));
        }
        return verbs;
    }

    private List<List<String>> convertToList(JSONArray posTagger) {
        List<List<String>> arr = new ArrayList<>();
        for (int i = 0; i < posTagger.size(); i++) {
            JSONArray temp = (JSONArray) posTagger.get(i);
            List<String> arrTmp = new ArrayList<>();
            for (int j = 0; j < temp.size(); j++) {
                arrTmp.add(temp.get(j).toString());
            }
            arr.add(arrTmp);
        }
        return arr;
    }

    private List<String> extractNumbers(List<List<String>> posTagger) {
        List<String> numbers = new ArrayList<>();
        for (List<String> pair : posTagger) {
            if (pair.get(1).equals("CD")) { // is a cardinal digit
                numbers.add(pair.get(0));
            }
        }
        return numbers;
    }

    private List<String> extractNouns(List<List<String>> posTagger) {
        List<String> nouns = new ArrayList<>();
        for (List<String> pair : posTagger) {
            if (pair.get(1).startsWith("NN")) { // is a noun
                nouns.add(pair.get(0));
            }
        }
        return nouns;
    }

    private void execute() throws FileNotFoundException {
        File targetDir = new File("src/main/java/com/bank");
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        HashMap<String, List<String>> analysisData = analysis.getMapMethods2Classes();
        NLPFileReader jsonResult = new NLPFileReader("src/main/resources/nlp_results.json");
        List<String> stepFiles = jsonResult.getFilenames();

        // {description = {matched class + used methods}}
        Map<String, List<String>> matchResult = match(analysisData, jsonResult, stepFiles.get(0));

    }

    public static void main(String[] args) throws FileNotFoundException {
        new Matcher().execute();
    }
}
