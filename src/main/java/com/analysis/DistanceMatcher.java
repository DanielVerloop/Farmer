package com.analysis;

import com.analysis.structures.Rule;
import com.analysis.structures.Scenario;
import com.analysis.structures.steps.*;
import com.analysis.util.Advice;
import com.analysis.util.ParameterParser;
import com.analysis.util.StringFormatter;
import com.analysis.util.distance.DiscoStringSimilarity;
import com.github.javaparser.ast.body.Parameter;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.WrongWordspaceTypeException;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Text-to-Code Matcher
 * This class uses String distance as base for matching text to code
 */
public class DistanceMatcher implements Matcher{
    private DiscoStringSimilarity model = new DiscoStringSimilarity();
    private Cosine cosine = new Cosine();//All other text-code matching
    private NormalizedLevenshtein levenshtein = new NormalizedLevenshtein();//Used for class matching
    private ParameterParser parameterParser = new ParameterParser(new File("src/test/resources/features/vendingMachine.feature"));//TODO: move instantiation to constructor!
    private List<Scenario> match;

    @Override
    public List<Scenario> getMatch() {
        return match;
    }

    public DistanceMatcher(File targetDir, List<Scenario> scenarios) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        match = this.match(scenarios, analysis);
    }

    @Override
    public List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        for (Scenario scenario : scenarios) {
            List<Rule> context = new ArrayList<>();
            List<Step> steps = scenario.getSteps();
            for (Step step : steps) {
                if (step instanceof GivenStep) {
                    Rule result = matchGiven((GivenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                    if (step.getAndSteps() != null && step.getAndSteps().size() > 0) {
                        for (Step sp: step.getAndSteps()) {
                            Rule andResult = matchAnd(sp, analysis, context);
                            sp.setMatchResult(andResult);
                            context.add(result);
                        }
                    }
                } else if (step instanceof WhenStep) {
                    Rule result = matchWhen((WhenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                    if (step.getAndSteps() != null && step.getAndSteps().size() > 0) {
                        for (Step sp: step.getAndSteps()) {
                            Rule andResult = matchWhen((WhenStep) sp, analysis, context);
                            sp.setMatchResult(andResult);
                            context.add(result);
                        }
                    }
                } else if (step instanceof ThenStep) {
                    Rule result = matchThen((ThenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.add(result);
                    if (step.getAndSteps() != null && step.getAndSteps().size() > 0) {
                        for (Step sp: step.getAndSteps()) {
                            Rule andResult = matchThen((ThenStep) sp, analysis, context);
                            sp.setMatchResult(andResult);
                            context.add(result);
                        }
                    }
                } else {
                    throw new IllegalStateException("Unsupported step type!");
                }
            }
        }
        return scenarios;
    }

    private Rule matchAnd(Step step, CodeAnalysis analysis, List<Rule> context) throws FileNotFoundException {
        String bestMatchedClass = findBestMatch(matchClass(step.getNouns(), analysis.getMapMethods2Classes()));

        String constructorResolver = MatchConstructor(bestMatchedClass, analysis, step);

        if (constructorResolver == null) {//something went wrong, default to no params
            new Rule(Advice.OBJECTI, bestMatchedClass, step.getParameters());
        } else if (constructorResolver.equals("")) {//no param-constructor
            new Rule(Advice.OBJECTI, bestMatchedClass, step.getParameters());
        }
        // we were able to match parameters with a constructor
        return new Rule(Advice.OBJECTI, bestMatchedClass, step.getParameters(), constructorResolver);
    }

    private Rule matchGiven(Step step, CodeAnalysis analysis, List<Rule> context) throws FileNotFoundException {
        //Matched class
        String bestMatchedClass = findBestMatch(matchClass(step.getNouns(), analysis.getMapMethods2Classes()));

        String constructorResolver = MatchConstructor(bestMatchedClass, analysis, step);
        if (constructorResolver == null) {//something went wrong, default to no params
            new Rule(Advice.OBJECTI, bestMatchedClass, step.getParameters());
        } else if (constructorResolver.equals("")) {//no param-constructor
            new Rule(Advice.OBJECTI, bestMatchedClass, step.getParameters());
        }
        // we were able to match parameters with a constructor
        return new Rule(Advice.OBJECTI, bestMatchedClass, step.getParameters(), constructorResolver);
    }

    private Rule matchWhen(WhenStep step, CodeAnalysis analysis, List<Rule> context) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        //TODO: for each verb analyse using srl
        // use ARG0, ARG1 and ARG2 to see what method is supposed to do
        // then use this to suggest method calls
        //Get class from given step
        String matchedClass = context.get(0).getClassName();//get class from given-step
        //get the methods of the most common class
        List<String> methods = analysis.getMapMethods2Classes().get(matchedClass);
        //find the closest method
        String matchedMethod = matchMethod(step.getAdvice(), step.getVerbs(), step, analysis, matchedClass);

        //Add parameters
        //TODO: check ordering of parameters!
        List<String> param = new ArrayList<>();
        if (step.getParameters().size() > 0){
            for (String parameter : step.getParameters()) {
                param.add(parameter);
            }
        }
        if (step.getNumbers().size() > 0) { // numbers are present
            for (String number : step.getNumbers()) {
                param.add(number);
            }
        }

        return new Rule(Advice.METHODI,matchedClass, param, matchedMethod);
    }

    private Rule matchThen(ThenStep step, CodeAnalysis analysis, List<Rule> context) throws WrongWordspaceTypeException, IOException, CorruptConfigFileException {
        String compareValue = "";
        String assertStmt = "equals";
        List<String> parameters = new ArrayList<>();
        String matchedClass = context.get(0).getClassName();

        if (step.getAdvice() == null) {
            return new Rule(Advice.Pass, context.get(0).getClassName(), "", compareValue, assertStmt);
        }
        //See if we can find a matching class field or method (if field does not exist)
        String fieldName = matchVar(analysis, step.getNouns());
        String methodName = matchGet(step.getAdvice(), step.getVerbs(), step, analysis, matchedClass);

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
                //TODO: check type of parameter for comparator
                methodName = matchGet(step.getAdvice(), step.getVerbs(), step, analysis, matchedClass);
                if (Arrays.asList("double", "int").contains(step.getParent().getTypeSolver().getParameterType(compareValue))) {
                    assertStmt = "equals";
                } else {//string comparison
                    assertStmt = "sequals";
                }
            }
        }
        if (fieldName != null && fieldName != "") {
            return new Rule(Advice.ASSERT, context.get(0).getClassName(), fieldName, compareValue, assertStmt);
        }
        return new Rule(Advice.ASSERT, context.get(0).getClassName(), methodName, parameters, compareValue, assertStmt);
    }

    private String matchGet(Map<String, String> advice, Set<String> verbs, Step step, CodeAnalysis analysis, String className) throws WrongWordspaceTypeException, IOException {
        HashMap<String, Double> distances = new HashMap<>(); //{"cls+method" = dist}
        Set<String> methods = new HashSet<>();

        //find methods with correct return type
        if (step.getParameters().size() > 0) {
            for (String param : step.getParameters()) {
                methods.addAll(analysis.getMethodsWithReturnType(className,
                        step.getParent().getTypeSolver().getParameterType(param)));
            }
        }
        if (step.getNumbers().size() > 0) {
            methods.addAll(analysis.getMethodsWithReturnType(className, "int"));
            methods.addAll(analysis.getMethodsWithReturnType(className, "double"));
        }
        //the actual matching
        for (String verb : verbs) {
            String verbString = advice.get("V") + advice.get("ARG1");
            for (String method : methods) {
                String splitMethodName = new StringFormatter().splitMethodName(method);

                //add levenshtein similarity
                double dist = this.levenshtein.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(method, dist, Double::sum);
                }
                //add cosine similarity
                dist = this.cosine.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(method, dist, Double::sum);
                }
                //add second order similarity
                dist = this.model.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(method, dist, Double::sum);
                }
            }
        }

        // return best setMatchResult
        return distances.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
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
                    double dist = this.cosine.distance(var, noun);
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

    //TODO implement mathcer that uses all available numbers + params + advice + verbs + nouns
    private String matchMethod(Map<String, String> advice, Set<String> verbs, Step step, CodeAnalysis analysis, String matchedClass) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        Map<String, Double> distances = new HashMap<>();
        List<String> methods = analysis.getMapMethods2Classes().get(matchedClass);

        if (step.getParameters().size() > 0) {//tables are used
            ArrayList<String> paramTypes = new ArrayList<>();
            for (String param : step.getParameters()) {
                paramTypes.add(parameterParser.getParameterType(param));
            }
            //filter methods
            methods = analysis.filterMethodsOnParams(matchedClass, paramTypes);
            if (methods.size() == 0) {
                methods = analysis.getMapMethods2Classes().get(matchedClass);
            }
        } else if (step.getNumbers().size() > 0) {//numbers are present but no tables
            //TODO: filter methods on double, int parameters

        }
        //do the matching on the remaining methods
        for (String verb : verbs) {
            String verbString = advice.get("V") + advice.get("ARG1");
            for (String method : methods) {
                String splitMethodName = new StringFormatter().splitMethodName(method);

                //add levenshtein similarity
                double dist = this.levenshtein.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(method, dist, Double::sum);
                }
                //add cosine similarity
                dist = this.cosine.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(method, dist, Double::sum);
                }
                //add second order similarity
                dist = this.model.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(method, dist, Double::sum);
                }
            }
        }

        return distances.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
    }

    /**
     * Matches a class to a step using only nouns
     * @param nouns
     * @param codeAnalysis
     * @return class name
     */
    private HashMap<String, String> matchClass(List<String> nouns, HashMap<String, List<String>> codeAnalysis) {
        HashMap<String, String> bestMatchedClass = new HashMap<>(); // {word=[suggested class]}
        for (String noun : nouns) {
            double smallestDist = Integer.MAX_VALUE;
            String matchedClass = "";
            for (Map.Entry<String, List<String>> entry : codeAnalysis.entrySet()) {
                String s = entry.getKey();
                double dist = this.cosine.distance(s, noun);
                if (dist < smallestDist) {
                    smallestDist = dist;
                    matchedClass = s;
                }
            }
            bestMatchedClass.put(noun, matchedClass);
        }
        return bestMatchedClass;
    }

    /**
     * Matches which constructor of a class is used for object instantiation
     * TODO: test if method works as intended
     * @param bestMatchedClass
     * @param analysis
     * @param step
     * @return null if no match could be made, "" if we have no parameters in constructor, else constructor parameters
     */
    private String MatchConstructor(String bestMatchedClass, CodeAnalysis analysis, Step step) {
        //Get list of constructors
        List<String> paramParserResult = new ArrayList<>();
        List<List<Parameter>> constructors = analysis.getConstructors(bestMatchedClass);
        //get parameters and see if they are a primitive type (int, double, string) or we can match a class
        for (int i = 0; i < constructors.size(); i++) {
            if (constructors.get(i).size() == step.getParameters().size()) {
                //case no parameters
                if (constructors.get(i).size() == 0) {
                    return "";
                }
                //case 1 parameter
                if (constructors.get(i).size() == 1) {
                    if (constructors.get(i).get(0).getType().toString().equals(parameterParser.getParameterType(step.getParameters().get(0)))) {
                        return constructors.get(i).get(0).getType().toString();
                    } else if (constructors.get(i).get(0).getType().getChildNodes().size() == 2){ //List, Array, Set parameter
                        if (Arrays.asList("String", "int", "double").contains(constructors.get(i).get(0).getType().getChildNodes().get(1))){
                            String type = parameterParser.getParameterType(step.getParameters().get(0));

                            if (constructors.get(i).get(0).getType().getChildNodes().get(1).equals(type)) {
                                return constructors.get(i).get(0).getType().getChildNodes().get(0).toString() + type;
                            }
                        } else { //find constructor of class object and make recursive call
                            String objectName = constructors.get(i).get(0).getType().getChildNodes().get(1).toString();
                            System.out.println(MatchConstructor(objectName, analysis, step));
                        }
                    }
                }
                if (constructors.get(i).size() > 2) {//multiple parameters
                    //TODO: test
                    String result = "";
                    for (Parameter p : constructors.get(i)) {
                        result += MatchConstructor(p.getType().toString(), analysis, step);
                    }
                    System.out.println(result);
                    return result;
                }
            } else {
                //TODO: match on number values if no params present!
                if (step.getNumbers().size() > 0 && constructors.get(i).size() == step.getNumbers().size()) {
                    return constructors.get(i).get(0).getType().toString();
                }

            }
        }
        return null;
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

    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        List<Scenario> scenarios = new NLPFileReader("src/main/resources/nlp_results.json",
                "src/test/resources/features/vendingMachine.feature")
                .getScenarios("vendingMachine.feature");
        DistanceMatcher matcher = new DistanceMatcher(
                new File("src/main/java/com/vendingmachine"), scenarios);
    }
}
