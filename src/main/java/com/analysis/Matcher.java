package com.analysis;

import com.analysis.util.LevenshteinDistance;
import com.analysis.util.SRLAnalyzer;

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
            Map<String, List<String>> posResult =  json.getPos(stepFile, description);

            if (description.startsWith("Given")) {
                result.add(matchGiven(posResult, codeAnalysis));
            } else if (description.startsWith("When")) {
                Map<String, List<String>> srlSentence = json.getSrl(stepFile, description);
                result.add(matchWhen(posResult, srlSentence, codeAnalysis));
            } else if (description.startsWith("Then")) {
                Map<String, List<String>> srlSentence = json.getSrl(stepFile, description);
                result.add(matchThen(posResult, srlSentence, codeAnalysis));
                break;
            } else {
                System.out.println(description);
                throw new IllegalStateException("Unsupported description keyword");
            }
        }
        System.out.println(result);

        //TODO: return datastructure to generate code from
        // example =
        // {
        //   "description"={Advice="Object instantiation", Type="BankAccount", parameters={0}}
//           "description"={Advice="method call", Class="BankAccount", method="deposit", parameters={100}}
        // }
        return null;
    }

    /**
     * Helper function to analyze a single given-step
     * @param posResult pos-tagger info -> nouns, numbers, etc
     * @param codeAnalysis code analysis
     * @return info for generator to be able to generate code
     */
    private String matchGiven(Map<String, List<String>> posResult, HashMap<String, List<String>> codeAnalysis) {
//        HashMap<String, List<String>> result = new HashMap<>();
        List<String> nouns = posResult.get("nouns");
        List<String> numbers = posResult.get("numbers");

        //for each class compute levenshtein distance
        HashMap<String, String> bestMatchedClass = bestMatchedClass(nouns, codeAnalysis);

        //Matched class
        String className = findMostCommon(bestMatchedClass);
        //TODO:add parameters for object instantiation

        return className;
    }

    private String matchWhen(Map<String, List<String>> posResult, Map<String, List<String>> srlSentence, HashMap<String, List<String>> codeAnalysis) {
        //TODO: for each verb analyse using srl
        // use ARG0, ARG1 and ARG2 to see what method is supposed to do
        // then use this to suggest method calls
        List<String> nouns = posResult.get("nouns");
        List<String> numbers = posResult.get("numbers");
        Set<String> verbs = srlSentence.keySet();
        SRLAnalyzer analyzer = new SRLAnalyzer(srlSentence);
        Map<String, String> advice = analyzer.generateAdvice();

        //for each class compute levenshtein distance
        HashMap<String, String> matchedClasses = bestMatchedClass(nouns, codeAnalysis);
        //get the methods of the most common class
        List<String> methods = codeAnalysis.get(findMostCommon(matchedClasses));
        //find the closest method
        String matchedMethod = matchMethod(numbers, advice, verbs, methods);

        return matchedMethod;
    }

    private String matchThen(Map<String, List<String>> posResult, Map<String, List<String>> srlSentence, HashMap<String, List<String>> codeAnalysis) {
        //TODO: analyze on nouns, numbers and verbs
        List<String> nouns = posResult.get("nouns");
        List<String> numbers = posResult.get("numbers");
        Set<String> verbs = srlSentence.keySet();
        SRLAnalyzer analyzer = new SRLAnalyzer(srlSentence);
        Map<String, String> advice = analyzer.generateAdvice();

        // get "target" (ARG1) from srl and then find method to match it (high likelihood of class variable or getter)
        // comparison value is ARG2 (if number this equals numbers list)
        // as we are in then, we pobably select assert statement based on ARGM maybe (should be, lower, higher, etc)?
        // needs testing on other projects!


        return "";
    }

    /**
     * Match method to nlp analysis
     * @param numbers
     * @param advice
     * @param verbs
     * @param methods
     * @return method name
     */
    private String matchMethod(List<String> numbers, Map<String, String> advice, Set<String> verbs, List<String> methods) {
        //TODO implement mathcer that uses all 3
        //simple matcher on the verbs
        Map<String, Integer> distances = new HashMap<>();
        for (String verb : verbs) {
            int smallestDist = Integer.MAX_VALUE;
            String matchedMethod = "";
            for (String method : methods) {
                int dist = new LevenshteinDistance(verb, method).getDistance();
                if (dist < smallestDist) {
                    smallestDist = dist;
                    matchedMethod = method;
                }
            }
            distances.put(matchedMethod, smallestDist);
        }
        return distances.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
    }

    /**
     *
     * @param nouns
     * @param codeAnalysis
     * @return
     */
    private HashMap<String, String> bestMatchedClass(List<String> nouns, HashMap<String, List<String>> codeAnalysis) {
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
        return bestMatchedClass;
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
