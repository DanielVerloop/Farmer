package com.analysis;

import com.analysis.structures.Rule;
import com.analysis.structures.Scenario;
import com.analysis.structures.steps.*;
import com.analysis.util.Advice;
import com.analysis.util.distance.LevenshteinDistance;
import info.debatty.java.stringsimilarity.Levenshtein;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Text-to-Code Matcher
 * This class should be able to setMatchResult functions to the step descriptions
 */
public class LevenshteinMatcher implements Matcher{
    private List<Scenario> match;

    @Override
    public List<Scenario> getMatch() {
        return match;
    }

    public LevenshteinMatcher(File targetDir, List<Scenario> scenarios) throws FileNotFoundException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        match = this.match(scenarios, analysis);
    }

    @Override
    public List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) {
        for (Scenario scenario : scenarios) {
            List<Rule> context = new ArrayList<>();
            List<Step> steps = scenario.getSteps();
            int i = 4;
            for (Step step : steps) {
                //TODO: add And Step support!
                if (GivenStep.class.equals(step.getClass())) {
                    Rule result = matchGiven((GivenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                } else if (WhenStep.class.equals(step.getClass())) {
                    Rule result = matchWhen((WhenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                } else if (ThenStep.class.equals(step.getClass())) {
                    Rule result = matchThen((ThenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                } else if (AndStep.class.equals(step.getClass())) {
                    Rule result = null;
                    result = MatchAnd((AndStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                    i++;
                } else {
                    throw new IllegalStateException("Unsupported step type!");
                }
            }
        }
        return scenarios;
    }

    //TODO: implement
    private Rule MatchAnd(AndStep step, CodeAnalysis analysis, List<Rule> context) {
        if (GivenStep.class.equals(((AndStep) step).getLinkedStep().getClass())) {
            return matchGiven(step, analysis, context);
        } else if (WhenStep.class.equals(((AndStep) step).getLinkedStep().getClass())) {
            //Get class from given step
            String matchedClass = context.get(0).getClassName();//get class from given-step
            //get the methods of the most common class
            List<String> methods = analysis.getMapMethods2Classes().get(matchedClass);
            //find the closest method
            String matchedMethod = matchMethod(step.getNumbers(), step.getAdvice(), step.getVerbs(), methods);

            List<String> param = new ArrayList<>();
            if (step.getNumbers().size() > 0) {
                param.add(step.getNumbers().get(0));
            } else {
                param = null;
            }

            return new Rule(Advice.METHODI, matchedClass, param, matchedMethod);
        } else if (ThenStep.class.equals(((AndStep) step).getLinkedStep().getClass())) {
            return new Rule(Advice.ASSERT, context.get(0).getClassName(), "something", "0", "equals");
        } else {
            throw new IllegalStateException("Unsupported step type!");
        }
    }

    private Rule matchGiven(Step step, CodeAnalysis analysis, List<Rule> context) {
        //for each class compute levenshtein distance
        HashMap<String, String> bestMatchedClass = matchClass(step.getNouns(), analysis.getMapMethods2Classes());
        
        //Matched class
        String className = findBestMatch(bestMatchedClass);

        //TODO:add parameters for object instantiation
//        String params = new StringFormatter().parseParameters();

        return new Rule(Advice.OBJECTI, className, step.getParameters());
    }

    private Rule matchWhen(WhenStep step, CodeAnalysis analysis, List<Rule> context) {
        //TODO: for each verb analyse using srl
        // use ARG0, ARG1 and ARG2 to see what method is supposed to do
        // then use this to suggest method calls

        //Get class from given step
        String matchedClass = context.get(0).getClassName();//get class from given-step
        //get the methods of the most common class
        List<String> methods = analysis.getMapMethods2Classes().get(matchedClass);
        //find the closest method
        String matchedMethod = matchMethod(step.getNumbers(), step.getAdvice(), step.getVerbs(), methods);

        List<String> param = new ArrayList<>();
        if (step.getNumbers().size() > 0) {
            param.add(step.getNumbers().get(0));
        } else {
            param = null;
        }

        return new Rule(Advice.METHODI,matchedClass, param, matchedMethod);
    }

    private Rule matchThen(ThenStep step, CodeAnalysis analysis, List<Rule> context) {
        String compareValue = "";
        String assertStmt = "";
        List<String> parameters = new ArrayList<>();

        if (step.getAdvice() == null) {
            return new Rule(Advice.Pass, context.get(0).getClassName(), "", compareValue, assertStmt);
        }
        //See if we can find a matching class field or method (if field does not exist)
        String fieldName = matchVar(analysis, step.getNouns());
        String methodName = matchGet(step.getNumbers(), step.getNouns(), analysis);

        if (step.getNumbers().size() == 1) {
            //highly likely a number parameter
            //if arg2 refers to the number then this is highly likely used in assert statement
            if (step.getAdvice().get("ARG2") != null) {
                if (step.getAdvice().get("ARG2").contains(step.getNumbers().get(0))) {
                    compareValue = step.getNumbers().get(0);
                }
                //Get the type of assert statement
                if (step.getAdvice().get("ARG2").contains("higher")) assertStmt = "higher";
                else if (step.getAdvice().get("ARG2").contains("lower")) assertStmt = "lower";
                else if (step.getAdvice().get("V").equals("be") || step.getAdvice().get("ARG2").contains(" equal")) {
                    assertStmt = "equals";
                }
            } else {
                //TODO: use arg1 and arg0
                assertStmt = "equals";
                compareValue = "something";
            }

        } else if (step.getNumbers().size() > 1) {
            //a lot of number parameters
            //TODO: find example to implement logic

        } else {
            //no numbers
            //TODO: use verb with arg1
            if (step.getParameters().size() > 0) {//if we use tables
                List<String> params = step.getParameters();
                String[] arg1 = step.getAdvice("ARG1").split("\\s");
                for (int i = 0; i < arg1.length; i++) {
                    for (int j = 0; j < params.size(); j++) {
                        if (arg1[i].equals(params.get(j))) {
                            //we know we have to look at table values
                            compareValue = arg1[i];
                            parameters.add(arg1[i]);
                            break;
                        }
                    }
                }
                //TODO: find a matching method
                methodName = "get" + compareValue;
            }
        }
        if (fieldName != null || fieldName != "") {
            return new Rule(Advice.ASSERT, context.get(0).getClassName(), fieldName, compareValue, assertStmt);
        }
        return new Rule(Advice.ASSERT, context.get(0).getClassName(), methodName, parameters, compareValue, assertStmt);
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
        Map<String, Double> distances = new HashMap<>();
        HashMap<String, List<String>> classVariables = codeAnalysis.getClassFields();
        HashMap<String, List<String>> classToMethods = codeAnalysis.getMapMethods2Classes();

        //first look if we can setMatchResult a class variable
        //TODO: test on bigger target classes
        classVariables.forEach((s, vars) -> {
            for (String noun : nouns) {
                double smallestDist = Integer.MAX_VALUE;
                String matchedVar = "";
                for (String var: vars) {
                    double dist = new Levenshtein().distance(var, noun);
                    if (dist < smallestDist) {
                        smallestDist = dist;
                        matchedVar = var;
                    }
                }
                distances.put(matchedVar, smallestDist);
            }
        });
        return distances.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
    }

    private String matchMethod(List<String> numbers, Map<String, String> advice, Set<String> verbs, List<String> methods) {
        //TODO implement mathcer that uses all 3
        //simple matcher on the verbs
        Map<String, Double> distances = new HashMap<>();
        for (String verb : verbs) {
            double smallestDist = Integer.MAX_VALUE;
            String matchedMethod = "";
            for (String method : methods) {
                double dist = new Levenshtein().distance(verb, method);
                if (dist < smallestDist) {
                    smallestDist = dist;
                    matchedMethod = method;
                }
            }
            distances.put(matchedMethod, smallestDist);
        }
        return distances.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
    }

    private HashMap<String, String> matchClass(List<String> nouns, HashMap<String, List<String>> codeAnalysis) {
        HashMap<String, String> bestMatchedClass = new HashMap<>(); // {word=[suggested class]}
        for (String noun : nouns) {
            double smallestDist = Integer.MAX_VALUE;
            String matchedClass = "";
            for (Map.Entry<String, List<String>> entry : codeAnalysis.entrySet()) {
                String s = entry.getKey();
                double dist = new Levenshtein().distance(s, noun);
                if (dist < smallestDist) {
                    smallestDist = dist;
                    matchedClass = s;
                }
            }
            bestMatchedClass.put(noun, matchedClass);
        }
        return bestMatchedClass;
    }

    private String findBestMatch(HashMap<String, String> bestMatchedClass) {
        //if no parameters match only on nouns
        Map<String, Long> counter = bestMatchedClass
                .values()
                .stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));


        String bestMatch = counter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();
        return bestMatch;
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<Scenario> scenarios = new NLPFileReader("src/main/resources/nlp_results.json")
                .getScenarios("vendingMachine.feature");
        LevenshteinMatcher matcher = new LevenshteinMatcher(
                new File("src/main/java/com/vendingmachine"), scenarios);

        System.out.println(matcher.getMatch());

    }
}
