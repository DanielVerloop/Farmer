package com.analysis;

import com.analysis.structures.MatchHandler;
import com.analysis.structures.Scenario;
import com.analysis.structures.steps.GivenStep;
import com.analysis.structures.steps.Step;
import com.analysis.structures.steps.ThenStep;
import com.analysis.structures.steps.WhenStep;
import com.analysis.util.Advice;
import com.analysis.util.LevenshteinDistance;
import com.analysis.util.StringFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Text-to-Code Matcher
 * This class should be able to setMatchResult functions to the step descriptions
 */
public class Matcher {
    private List<Scenario> match;

    public List<Scenario> getMatch() {
        return match;
    }

    public Matcher(File targetDir, List<Scenario> scenarios) throws FileNotFoundException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        match = this.match(scenarios, analysis);
    }

    public List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) {
        //init VisitorHandler
        MatchHandler handler = new MatchHandler();

        for (Scenario scenario : scenarios) {
            Map<Step, List<String>> context = new HashMap<>();
            List<Step> steps = scenario.getSteps();
            for (Step step : steps) {
                //TODO: add And Step support!
                if (GivenStep.class.equals(step.getClass())) {
                    List<String> result = matchGiven((GivenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.put(step, result);
                } else if (WhenStep.class.equals(step.getClass())) {
                    List<String> result;
                    result = matchWhen((WhenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.put(step, result);
                } else if (ThenStep.class.equals(step.getClass())) {
                    List<String> result;
                    result = matchThen((ThenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.put(step, result);
                } else {
                    throw new IllegalStateException("unsupported step type!");
                }
            }
        }
        //TODO: return datastructure to generate code from
        // example =
        // {
        //   "description"={Advice="Object instantiation", Type="BankAccount", parameters={0}}
        //   "description"={Advice="method call", Class="BankAccount", method="deposit", parameters={100}}
        // }
        return scenarios;
    }

    private List<String> matchGiven(GivenStep step, CodeAnalysis analysis, Map<Step, List<String>> context) {
        //for each class compute levenshtein distance
        HashMap<String, String> bestMatchedClass = bestMatchedClass(step.getNouns(), analysis.getMapMethods2Classes());

        //Matched class
        String className = findMostCommon(bestMatchedClass);

        //TODO:add parameters for object instantiation
        String params = new StringFormatter().parseParameters(step.getNumbers());

        return new ArrayList<>(Arrays.asList(Advice.OBJECTI.toString(), className, params));
    }

    private List<String> matchWhen(WhenStep step, CodeAnalysis analysis, Map<Step, List<String>> context) {
        //TODO: for each verb analyse using srl
        // use ARG0, ARG1 and ARG2 to see what method is supposed to do
        // then use this to suggest method calls
        //TODO: use context to infer used classes!
        HashMap<String, List<String>> codeAnalysis = analysis.getMapMethods2Classes();
        //for each class compute levenshtein distance
        HashMap<String, String> matchedClasses = bestMatchedClass(step.getNouns(), codeAnalysis);
        //get the methods of the most common class
        List<String> methods = codeAnalysis.get(findMostCommon(matchedClasses));
        //find the closest method
        String matchedMethod = matchMethod(step.getNumbers(), step.getAdvice(), step.getVerbs(), methods);

        return new ArrayList<>(Arrays.asList(Advice.METHODI.toString(), matchedMethod, findMostCommon(matchedClasses), step.getNumbers().get(0)));
    }


    private List<String> matchThen(ThenStep step, CodeAnalysis analysis, Map<Step, List<String>> context) {
        String compareValue = "";
        String assertStmt = "";

        //Create a list of all to be checked nouns
        List<String> checkNouns = Arrays.stream(step.getAdvice().get("target").split(" "))
                .distinct()
                .filter(step.getNouns()::contains)
                .collect(Collectors.toList());
        //See if we can find a matching class field or method (if field does not exist)
        String fieldName = matchVar(analysis, checkNouns);
        String methodName = matchGet(step.getNumbers(), checkNouns, analysis);

        if (step.getNumbers().size() == 1) {
            //highly likely a number parameter
            //if arg2 refers to the number then this is highly likely used in assert statement
            if (step.getAdvice().get("ARG2").contains(step.getNumbers().get(0))) {
                compareValue = step.getNumbers().get(0);
            }
            //Get the type of assert statement
            if (step.getAdvice().get("ARG2").contains("higher")) assertStmt = "higher";
            else if (step.getAdvice().get("ARG2").contains("lower")) assertStmt = "lower";
            else if (step.getAdvice().get("action").equals("be") || step.getAdvice().get("ARG2").contains(" equal")) {
                assertStmt = "equals";
            }

        } else if (step.getNumbers().size() > 1) {
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

        // return best setMatchResult
        return distances.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
    }

    private String matchVar(CodeAnalysis codeAnalysis, List<String> nouns) {
        Map<String, Integer> distances = new HashMap<>();
        HashMap<String, List<String>> classVariables = codeAnalysis.getClassFields();
        HashMap<String, List<String>> classToMethods = codeAnalysis.getMapMethods2Classes();

        //first look if we can setMatchResult a class variable
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


    public static void main(String[] args) throws FileNotFoundException {
        List<Scenario> scenarios = new NLPFileReader("src/main/resources/nlp_results_new.json").getScenarios("transactions.feature");
        Matcher matcher = new Matcher(new File("src/main/java/com/bank"), scenarios);

        System.out.println(matcher.getMatch());

    }
}
