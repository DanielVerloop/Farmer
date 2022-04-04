package com.analysis;

import com.analysis.util.Advice;
import com.analysis.util.LevenshteinDistance;
import com.analysis.util.SRLAnalyzer;
import com.analysis.util.StringFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Text-to-Code Matcher
 * This class should be able to match functions to the step descriptions
 */
public class Matcher {
    private Map<String, List<List<String>>> match;

    public Map<String, List<List<String>>> getMatch() {
        return match;
    }

    public Matcher(File targetDir, NLPFileReader jsonResult) throws FileNotFoundException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        match = this.match(analysis, jsonResult, jsonResult.getFilenames().get(0));
    }
    /**
     * Matches all descriptions to code
     * TODO: idea separate matchThen, when, given into separate classes with interface
     * @return mapping of filename + list
     */
    public Map<String, List<List<String>>> match(CodeAnalysis codeAnalysis, NLPFileReader json, String stepFile) {
        String[] descriptions = json.getSteps(stepFile).toArray(new String[0]);
        Map<String, List<List<String>>> fileResult = new HashMap<>();
        List<List<String>> result = new ArrayList<>();


        for (String description : descriptions) {
            Map<String, List<String>> posResult =  json.getPos(stepFile, description);

            if (description.startsWith("Given")) {
                result.add(matchGiven(posResult, codeAnalysis.getMapMethods2Classes()));
            } else if (description.startsWith("When")) {
                Map<String, List<String>> srlSentence = json.getSrl(stepFile, description);
                result.add(matchWhen(posResult, srlSentence, codeAnalysis.getMapMethods2Classes()));
            } else if (description.startsWith("Then")) {
                Map<String, List<String>> srlSentence = json.getSrl(stepFile, description);
                result.add(matchThen(posResult, srlSentence, codeAnalysis));
            } else {
                System.out.println(description);
                throw new IllegalStateException("Unsupported description keyword");
            }
        }
        //TODO: return datastructure to generate code from
        // example =
        // {
        //   "description"={Advice="Object instantiation", Type="BankAccount", parameters={0}}
        //   "description"={Advice="method call", Class="BankAccount", method="deposit", parameters={100}}
        // }
        fileResult.put(stepFile, result);
        return fileResult;
    }

    /**
     * Helper function to analyze a single given-step
     * @param posResult pos-tagger info -> nouns, numbers, etc
     * @param codeAnalysis code analysis
     * @return info for generator to be able to generate code
     */
    private List<String> matchGiven(Map<String, List<String>> posResult, HashMap<String, List<String>> codeAnalysis) {
//        HashMap<String, List<String>> result = new HashMap<>();
        List<String> nouns = posResult.get("nouns");
        List<String> numbers = posResult.get("numbers");

        //for each class compute levenshtein distance
        HashMap<String, String> bestMatchedClass = bestMatchedClass(nouns, codeAnalysis);

        //Matched class
        String className = findMostCommon(bestMatchedClass);
        //TODO:add parameters for object instantiation
        String params = new StringFormatter().parseParameters(numbers);

        return new ArrayList<>(Arrays.asList(Advice.OBJECTI.toString(), className, params));
    }

    private List<String> matchWhen(Map<String, List<String>> posResult, Map<String, List<String>> srlSentence, HashMap<String, List<String>> codeAnalysis) {
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

        return new ArrayList<>(Arrays.asList(Advice.METHODI.toString(), matchedMethod, findMostCommon(matchedClasses), numbers.get(0)));
    }

    private List<String> matchThen(Map<String, List<String>> posResult, Map<String, List<String>> srlSentence, CodeAnalysis codeAnalysis) {
        //TODO: analyze on numbers and verbs too
        List<String> nouns = posResult.get("nouns");
        List<String> numbers = posResult.get("numbers");
        Set<String> verbs = srlSentence.keySet();
        SRLAnalyzer analyzer = new SRLAnalyzer(srlSentence);
        Map<String, String> advice = analyzer.generateAdvice();

        // get "target" (ARG1) from srl and then find method to match it (high likelihood of class variable or getter)
        // comparison value is ARG2 (if number this equals numbers list)
        // as we are in then, we probably select assert statement based on ARGM maybe (should be, lower, higher, equal, etc)?
        // needs testing on other projects!
        String compareValue = "";
        String assertStmt = "";

        //Create a list of all to be checked nouns
        List<String> checkNouns = Arrays.stream(advice.get("target").split(" "))
                .distinct()
                .filter(nouns::contains)
                .collect(Collectors.toList());
        //See if we can find a matching class field or method (if field does not exist)
        String fieldName = matchVar(codeAnalysis, checkNouns);
        String methodName = matchGet(numbers, checkNouns, codeAnalysis);

        if (numbers.size() == 1) {
            //highly likely a number parameter
            //if arg2 refers to the number then this is highly likely used in assert statement
            if (advice.get("ARG2").contains(numbers.get(0))) {
                compareValue = numbers.get(0);
            }
            //Get the type of assert statement
            if (advice.get("ARG2").contains("higher")) assertStmt = "higher";
            else if (advice.get("ARG2").contains("lower")) assertStmt = "lower";
            else if (advice.get("action").equals("be") || advice.get("ARG2").contains(" equal")) {
                assertStmt = "equals";
            }

        } else if (numbers.size() > 1) {
            //a lot of number parameters
            //TODO: implement

        } else {
            //no numbers
            //TODO: implement
        }
        if (fieldName != null) {
            return new ArrayList<>(Arrays.asList(Advice.ASSERT.toString(), fieldName, compareValue, assertStmt));

        }
        return new ArrayList<>(Arrays.asList(Advice.ASSERT.toString(), methodName, compareValue, assertStmt));
    }

    private String matchGet(List<String> numbers, List<String> nouns, CodeAnalysis analysis) {
        HashMap<String, List<String>> mapping = analysis.getMapMethods2Classes();
        HashMap<String, Integer> distances = new HashMap<>(); //{"cls+method" = dist}
        //get best matched method per class
        List<String> methods = new ArrayList<>();
        mapping.forEach((cls, mtds) -> {
            String methodName = "";
            int smallestdist = Integer.MAX_VALUE;
            for (String noun : nouns) {
                for (String method : mtds) {
                    int ldist = new LevenshteinDistance(noun, method).getDistance();
                    //add bias on getters
                    if (ldist == smallestdist) {
                        if (method.contains("get")) {
                            ldist -= 3;
                        }
                    }
                    if (ldist < smallestdist) {
                        smallestdist = ldist;
                        methodName = method;
                    }
                }
            }
            distances.put(methodName, smallestdist);
        });

        // return best match
        return distances.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
    }

    private String matchVar(CodeAnalysis codeAnalysis, List<String> nouns) {
        Map<String, Integer> distances = new HashMap<>();
        HashMap<String, List<String>> classVariables = codeAnalysis.getClassFields();
        HashMap<String, List<String>> classToMethods = codeAnalysis.getMapMethods2Classes();

        //first look if we can match a class variable
        //TODO: test on bigger target classes
        classVariables.forEach((s, vars) -> {
            for (String noun : nouns) {
                int smallestDist = Integer.MAX_VALUE;
                String matchedVar = "";
                for (String var: vars) {
                    int dist = new LevenshteinDistance(var, noun).getDistance();
                    if (dist < smallestDist) {
                        smallestDist = dist;
                        matchedVar = var;
                    }
                }
                distances.put(matchedVar, smallestDist);
            }
        });
        return distances.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
    }

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

//    private void execute() throws FileNotFoundException {
//        File targetDir = new File("src/main/java/com/bank");
//        CodeAnalysis analysis = new CodeAnalysis(targetDir);
//        NLPFileReader jsonResult = new NLPFileReader("src/main/resources/nlp_results.json");
//        List<String> stepFiles = jsonResult.getFilenames();
//
//        // {description = {matched class + used methods}}
//        Map<String, List<String>> matchResult =
//
//    }
//
//    public static void main(String[] args) throws FileNotFoundException {
//        new Matcher().execute();
//    }
}
