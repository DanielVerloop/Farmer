package com.analysis;

import com.analysis.structures.Context;
import com.analysis.structures.Rule;
import com.analysis.structures.Scenario;
import com.analysis.structures.parameter.Constructor;
import com.analysis.structures.parameter.Method;
import com.analysis.structures.parameter.ParameterPair;
import com.analysis.structures.steps.*;
import com.analysis.util.*;
import com.analysis.util.distance.DiscoStringSimilarity;
import com.github.javaparser.ast.body.Parameter;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.WrongWordspaceTypeException;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Text-to-Code Matcher
 * This class uses String distance as base for matching text to code
 */
public class DistanceMatcher implements Matcher{
    private final DiscoStringSimilarity model;
    private final Cosine cosine;//All other text-code matching
    private final NormalizedLevenshtein levenshtein;//Used for class matching
    private final ParameterParser parameterParser;
    private final List<Scenario> match;

    @Override
    public List<Scenario> getMatch() {
        return match;
    }

    public DistanceMatcher(File targetDir, List<Scenario> scenarios, String featureFile) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        this.model = new DiscoStringSimilarity();
        this.cosine = new Cosine();
        this.levenshtein = new NormalizedLevenshtein();
        this.parameterParser = new ParameterParser(new File(featureFile));
        this.match = this.match(scenarios, analysis);
    }

    @Override
    public List<Scenario> match(List<Scenario> scenarios, CodeAnalysis analysis) throws IOException, WrongWordspaceTypeException {
        for (Scenario scenario : scenarios) {
            Context context = new Context();
            List<Step> steps = scenario.getSteps();
            for (Step step : steps) {
                if (step instanceof GivenStep) {
                    List<Rule> result = matchGiven(step, analysis, context);
                    step.setMatchResult(result);
                    context.addMatchingRule(result);
                    context.setMainClass(result.get(0).getClassName());
                    if (step.getAndSteps() != null && step.getAndSteps().size() > 0) {
                        for (Step sp: step.getAndSteps()) {
                            List<Rule> andResult = matchAnd((AndStep) sp, analysis, context);
                            sp.setMatchResult(andResult);
                            context.addMatchingRule(result);
                        }
                    }
                } else if (step instanceof WhenStep) {
                    List<Rule> result = matchWhen((WhenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.addMatchingRule(result);
                    if (step.getAndSteps() != null && step.getAndSteps().size() > 0) {
                        for (Step sp: step.getAndSteps()) {
                            List<Rule> andResult = matchWhen((WhenStep) sp, analysis, context);
                            sp.setMatchResult(andResult);
                            context.addMatchingRule(result);
                        }
                    }
                } else if (step instanceof ThenStep) {
                    List<Rule> result = matchThen((ThenStep) step, analysis, context);
                    step.setMatchResult(result);
                    context.addMatchingRule(result);
                    if (step.getAndSteps() != null && step.getAndSteps().size() > 0) {
                        for (Step sp: step.getAndSteps()) {
                            List<Rule> andResult = matchThen((ThenStep) sp, analysis, context);
                            sp.setMatchResult(andResult);
                            context.addMatchingRule(result);
                        }
                    }
                } else {
                    throw new IllegalStateException("Unsupported step type!");
                }
            }
        }
        return scenarios;
    }

    private List<Rule> matchAnd(AndStep step, CodeAnalysis analysis, Context context) throws WrongWordspaceTypeException, IOException {
        List<Rule> matchResult =new ArrayList<>();
        List<String> bestMatchedClasses =  matchClass(step.getNouns(), analysis,
                analysis.getMapMethods2Classes().keySet().size()/2);
        Constructor constructorResolver = matchConstructor(bestMatchedClasses, analysis, step);
        String bestMatchedClass = constructorResolver.getName();

        //check if we need to use method calls or object instantiations to couple the and step to given step.
        if (context.getMainClass().equals(bestMatchedClass)) {//method calls only
            String matchedMethod = matchMethod(step.getSrlLabels(), step.getVerbs(), step, analysis, bestMatchedClass).getName();
            List<String> param = new ArrayList<>();
            if (step.getParameters().size() > 0){
                param.addAll(step.getParameters());
            }
            //TODO:find more organized way of transforming numbers to parameters
            if (step.getNumbers().size() > 0) { // numbers are present
                for (int i = 0; i < step.getNumbers().size(); i++) {
                    param.add("arg"+i);
                }
            }

            return Collections.singletonList(new Rule(Advice.METHODI, bestMatchedClass, param, matchedMethod));
        }
        //for ordering of code add class instantiation first
        if (constructorResolver.getParameters().isEmpty()) {//no param-constructor
            matchResult.add(new Rule(Advice.OBJECTI, bestMatchedClass, null));
        } else {
            // we were able to match parameters with a constructor
            //Resolve mapping to correct parameter types
            List<String> parameters = getParams(analysis, constructorResolver.getParameters());
            matchResult.add(new Rule(Advice.OBJECTI, bestMatchedClass, parameters, "paramsUsed"));
        }
        //find way to couple the two classes
        String couplingMethod = matchCouplingMethod(step.getSrlLabels(), step.getVerbs(), step, analysis,
                context.getMainClass(), bestMatchedClass);
        if (!(couplingMethod == null)) {//if we found a method
            matchResult.add(new Rule(Advice.METHODI, context.getMainClass(),
                    Collections.singletonList(bestMatchedClass.toLowerCase()), couplingMethod));
        }
        return matchResult;
    }

    private List<Rule> matchGiven(Step step, CodeAnalysis analysis, Context context) {
        //Matched class
        List<String> bestMatchedClasses =  matchClass(step.getNouns(), analysis,
                analysis.getMapMethods2Classes().keySet().size()/2);
        Constructor constructorResolver = matchConstructor(bestMatchedClasses, analysis, step);
        String bestMatchedClass = constructorResolver.getName();

        //Get the matched parameter variables
        if (constructorResolver.getParameters().isEmpty()) {//no param-constructor
            return Collections.singletonList(new Rule(Advice.OBJECTI, bestMatchedClass, null));
        }
        // we were able to match parameters with a constructor
        //Resolve mapping to correct parameter types
        List<String> parameters = getParams(analysis, constructorResolver.getParameters());

        return Collections.singletonList(new Rule(Advice.OBJECTI, bestMatchedClass, parameters, "paramsUsed"));
    }

    private List<Rule> matchWhen(WhenStep step, CodeAnalysis analysis, Context context) throws IOException, WrongWordspaceTypeException {
        //Get class from given step
        String matchedClass = context.getMainClass();//get class from given-step
        //find the closest method
        Method matchedMethod = matchMethod(step.getSrlLabels(), step.getVerbs(), step, analysis, matchedClass);

        //list of parameter names in same ordering as types
        //list of types we have to match
        //TODO:find more organized way of transforming numbers to parameters
        List<String> targetTypes = new ArrayList<>();
        for (String param : step.getParameters()) { //use the ordering to remember names
            targetTypes.add(parameterParser.getParameterType(param));
        }
        List<String> parameterNames = new ArrayList<>(step.getParameters());
        ParameterTester tester = new ParameterTester();
        List<String> numbers = step.getNumbers();
        for (int i = 0; i < numbers.size(); i++) {
            String number = numbers.get(i);//care for floats
            String type = tester.returnNumberType(number);
            if (Arrays.asList("double", "int").contains(type)) {
                targetTypes.add(type);
                parameterNames.add("arg" + i);
            }
        }
        //if we can't find parameters for now generate without correct parameters
        if (targetTypes.size() == 0) {
            return Collections.singletonList(new Rule(Advice.METHODI, matchedClass, new ArrayList<>(), matchedMethod.getName()));
        }

        Map<String, String> parameterMapping = orderParameters(targetTypes, parameterNames, matchedMethod.getPair());
        List<String> parameters = getParams(analysis, parameterMapping);

        return Collections.singletonList(new Rule(Advice.METHODI, matchedClass, parameters, matchedMethod.getName()));
    }

    private List<Rule> matchThen(ThenStep step, CodeAnalysis analysis, Context context) throws WrongWordspaceTypeException, IOException {
        String compareValue = "";
        String assertStmt = "equals";
        List<String> parameters = new ArrayList<>();
        String matchedClass = context.getMainClass();

        if (step.getSrlLabels() == null) {
            return Arrays.asList(new Rule(Advice.Pass, context.getMainClass(), "", compareValue, assertStmt));
        }
        //See if we can find a matching class field or method (if field does not exist)
        String fieldName = matchVar(analysis, step.getNouns(), context);
        String methodName = matchGet(step.getSrlLabels(), step.getVerbs(), step, analysis, matchedClass);

        if (step.getNumbers().size() == 1) {
            //highly likely a number parameter
            //if arg2 refers to the number then this is highly likely the desired value
            if (step.getParameters().size() > 0) {
                for (String param : step.getParameters()) {
                    if (step.getSrlLabels().get("ARG2") != null &&
                            step.getSrlLabels().get("ARG2").contains(param)) {
                        compareValue += " + " + param;//test this
                    }
                    if (step.getSrlLabels().get("ARG1") != null && step.getSrlLabels().get("ARG1").contains(param)) {
                        parameters.add(param);
                    }
                }
            } else {
                if (step.getSrlLabels().get("ARG2") != null) {
                    if (step.getSrlLabels().get("ARG2").contains(step.getNumbers().get(0))) {
                        compareValue = "arg0";//TODO:find better way to process this
                    }
                    //Get the type of assert statement
                    if (step.getSrlLabels().get("ARG2").contains("higher")) assertStmt = "higher";
                    else if (step.getSrlLabels().get("ARG2").contains("lower")) assertStmt = "lower";
                    else if (step.getSrlLabels().get("V").equals("be") || step.getSrlLabels().get("ARG2").contains(" equal")) {
                        assertStmt = "equals";
                    }
                } else {
                    //TODO: use arg1
                    if (step.getSrlLabels().get("ARG1").contains(step.getNumbers().get(0))) {
                        compareValue = "arg0";
                        assertStmt = "equals";
                    }
                }
            }
        } else if (step.getNumbers().size() > 1) {
            //a lot of number parameters
            //TODO: find example to implement logic
            // most likely desired value is in ARG2 and parameters in ARG1

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
                methodName = matchGet(step.getSrlLabels(), step.getVerbs(), step, analysis, matchedClass);
                if (Arrays.asList("double", "int").contains(step.getParent().getTypeSolver().getParameterType(compareValue))) {
                    assertStmt = "equals";
                } else {//string comparison
                    assertStmt = "sequals";
                }
            } else {
                //default -> test will generate but no semantically correct code
                //TODO:implement this case
                compareValue = "true";
                assertStmt = "equals";
            }
        }
        if (fieldName != null && !fieldName.equals("")) {
            return Arrays.asList(new Rule(Advice.ASSERT, context.getMainClass(), fieldName, compareValue, assertStmt));
        }
        return Arrays.asList(new Rule(Advice.ASSERT, context.getMainClass(), methodName, parameters, compareValue, assertStmt));
    }

    /**
     * Match a method in the context of assert statement
     */
    private String matchGet(Map<String, String> advice, Set<String> verbs, Step step, CodeAnalysis analysis, String className) throws WrongWordspaceTypeException, IOException {
        HashMap<String, Double> distances = new HashMap<>(); //{"cls+method" = dist}
        Set<String> methods = new HashSet<>();

        //TODO: change to work with same approach as constructor and when matching
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
        if (methods.size() == 0) {//no numbers or params
            methods.addAll(analysis.getMethodsWithReturnType(className, "String"));
            methods.addAll(analysis.getMethodsWithReturnType(className, "boolean"));
        }
        //the actual matching
        for (String verb : verbs) {
            String verbString = advice.get("V") + " " + advice.get("ARG1");
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

    private String matchVar(CodeAnalysis codeAnalysis, List<String> nouns, Context context) {
        Map<String, Double> distances = new HashMap<>();
        HashMap<String, List<String>> classVariables = codeAnalysis.getClassFields();
        List<String> variables = classVariables.get(context.getMainClass());

        //first look if we can setMatchResult a class variable
        //TODO: returnNumberType on bigger target classes
        for (String noun : nouns) {
            double smallestDist = Integer.MAX_VALUE;
            String matchedVar = "";
            for (String var: variables) {
                double dist = this.levenshtein.distance(var, noun);
                if (dist < smallestDist) {
                    smallestDist = dist;
                    matchedVar = var;
                }
            }
            distances.put(matchedVar, smallestDist);
        }

        return distances.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
    }

    /**
     * Used to find a method that uses a second class as parameter
     */
    private String matchCouplingMethod(Map<String, String> srlLabels, Set<String> verbs, AndStep step, CodeAnalysis analysis, String matchedClass, String newClass) throws WrongWordspaceTypeException, IOException {
        Map<String, Double> distances = new HashMap<>();
        List<String> methods = analysis.getMethodsWithParamtype(matchedClass, newClass);
        if (methods.size() == 0) {
            return null;
        }
        if (methods.size() == 1) {
            return methods.get(0);
        }
        //do a srl/verb matching
        for (String verb : verbs) {
            String verbString = srlLabels.get("V") + srlLabels.get("ARG1");
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
     * Matches a method to a step
     */
    private Method matchMethod(Map<String, String> advice, Set<String> verbs, Step step, CodeAnalysis analysis, String matchedClass) throws IOException, WrongWordspaceTypeException {
        Map<Method, Double> distances = new HashMap<>();
        List<Method> methods = analysis.filterMethodsOnParams(matchedClass, null);

        if (step.getParameters().size() > 0) {//tables are used
            ArrayList<String> paramTypes = new ArrayList<>();
            for (String param : step.getParameters()) {
                paramTypes.add(parameterParser.getParameterType(param));
            }
            //filter methods
            methods = analysis.filterMethodsOnParams(matchedClass, paramTypes);
            if (methods.size() == 0) {
                methods = analysis.filterMethodsOnParams(matchedClass, null);
            }
        } else if (step.getNumbers().size() > 0) {//numbers are present but no tables
            List<String> types = new ArrayList<>();
            ParameterTester tester = new ParameterTester();
            for (String number: step.getNumbers()) {
                types.add(tester.returnNumberType(number));
            }
            methods = analysis.filterMethodsOnParams(matchedClass, types);
        }
        //do the matching on the remaining methods
        List<String> methodNames = new ArrayList<>();
        for (Method m : methods) {
            methodNames.add(m.getName());
        }
        for (String verb : verbs) {
            String verbString;
            //need bigger test set to validate the logic below
            if (advice.containsKey("ARG2")) {
                verbString = advice.get("ARG1") + " " + advice.get("V") + " " + advice.get("ARG2");
            } else {
                verbString = advice.get("V") + " " + advice.get("ARG1");
            }
            for (int i = 0; i < methodNames.size(); i++) {
                String method = methodNames.get(i);
                Method m = methods.get(i);
                String splitMethodName = new StringFormatter().splitMethodName(method);

                //add levenshtein similarity
                double dist = this.levenshtein.distance(verbString, splitMethodName);
                distances.merge(m, dist, Double::sum);
                //add cosine similarity
                dist = this.cosine.distance(verbString, splitMethodName);
                distances.merge(m, dist, Double::sum);
                //add second order similarity
                dist = this.model.distance(verbString, splitMethodName);
                if (dist >= 0 && dist <= 1) {
                    distances.merge(m, dist, Double::sum);
                } else {
                    distances.merge(m, 1.0, Double::sum);
                }
            }
        }

        return distances.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
    }

    /**
     * Matches a class to a step using only nouns
     */
    private List<String> matchClass(List<String> nouns, CodeAnalysis codeAnalysis, int n) {
        HashMap<String, Double> similarity = new HashMap<>(); // {word=[suggested class]}
        HashMap<String, Integer> matchCounter = new HashMap<>();
        for (String noun : nouns) {
            for (String className : codeAnalysis.getMapMethods2Classes().keySet()) {
                double dist = this.cosine.distance(className, noun) +
                        this.levenshtein.distance(className, noun);
                similarity.merge(className, dist, Double::sum);
                matchCounter.merge(className, 1, Integer::sum);
            }
        }
        matchCounter.forEach((s, counter) ->
            similarity.merge(s, 0.0, (a, b) -> (a+b)/counter)
        );

        HashMap<String, Double> sortedOnSim = sortByValue(similarity);
        List<String> result = new ArrayList<>();
        List<String> setIterable = new ArrayList<>(sortedOnSim.keySet());
        if (n == 0) n = 1;//edge case due to integer division
        for (int i = 0; i < n; i++) {
            result.add(setIterable.get(i));
        }
        return result;
    }

    private Constructor matchConstructor(List<String> bestMatchedClasses, CodeAnalysis analysis, Step step) {
        //first check if we have parameters to match
        if (step.getNumbers().size() == 0 && step.getParameters().size() == 0) {
            return new Constructor(bestMatchedClasses.get(0), new HashMap<>());
        }

        //list of parameter names in same ordering as types
        //list of types we have to match
        List<String> targetTypes = new ArrayList<>();
        for (String param : step.getParameters()) { //use the ordering to remember names
            targetTypes.add(parameterParser.getParameterType(param));
        }
        List<String> parameterNames = new ArrayList<>(step.getParameters());
        ParameterTester tester = new ParameterTester();
        List<String> numbers = step.getNumbers();
        for (int i = 0; i < numbers.size(); i++) {
            String number = numbers.get(i);//care for floats
            String type = tester.returnNumberType(number);
            if (Arrays.asList("double", "int").contains(type)) {
                targetTypes.add(type);
                parameterNames.add("arg" + i);
            }
        }

        //match the constructor
        for (String className : bestMatchedClasses ) {
            for (ParameterPair resolvedConstructor : analysis.constructorParamResolver(className)) {
                List<Parameter> baseTypeParameter = resolvedConstructor.getBaseParams();
                if (analysis.checkParamTypes(baseTypeParameter, targetTypes) &&
                    targetTypes.size() == baseTypeParameter.size()) {//correct param types

                    //loop over all possible combinations and match most likely param
                    Map<String, String> params = orderParameters(targetTypes, parameterNames, resolvedConstructor);
                    return new Constructor(className, params);
                }
            }
        }
        return new Constructor(bestMatchedClasses.get(0), new HashMap<>());
    }

    /**
     * Orders parameters in the correct order, outputs {name, type} pairs
     */
    private Map<String, String> orderParameters(List<String> parameterTypes, List<String> parameterNames, ParameterPair pair) {
        //loop over all possible combinations and match most likely param
        //TODO:find a stable parameter matcher
        String[] parameters = new String[parameterTypes.size()];
        for (int i = 0; i < parameterTypes.size(); i++) {
            double[] distances = new double[parameterTypes.size()];
            Arrays.fill(distances, 10);
            for (int j = 0; j < pair.getBaseParams().size(); j++) {
                if (parameterNames.get(i).startsWith("arg") && parameterNames.get(i).length() <= 5) {//matcher created variable
                    if (pair.getBaseParams().get(j).getTypeAsString().equals(parameterTypes.get(i))) {
                        distances[j] = 0.2;
                        continue;
                    }
                }
                if (pair.getBaseParams().get(j).getTypeAsString().equals(parameterTypes.get(i))) {
                    double stringDist = this.levenshtein.distance(
                            pair.getBaseParams().get(j).getNameAsString(), parameterNames.get(i));
                    distances[j] = stringDist;
                }
            }
            int[] sortedIndices = ArrayUtils.argsort(distances);
            for (int j = 0; j < sortedIndices.length; j++) {
                int index = sortedIndices[j];
                if (distances[index] != -1.0 && parameters[index] == null) {
                    parameters[index] = parameterNames.get(i);
                    break;
                }
            }

        }

        int original = 0;
        //LinkedHashMap is used to preserve insertion order
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < pair.getOriginalParams().size(); i++) {
            for (int j = i; j < pair.getBaseParams().size(); j++) {
                if (pair.getBaseParams().get(i).equals(pair.getOriginalParams().get(j))) {
                    for (int k = original; k < j; k++) {
                        params.put(parameters[k], pair.getOriginalParams().get(original).getTypeAsString());
                    }
                    params.put(parameterNames.get(i), pair.getOriginalParams().get(i).getTypeAsString());
                    original = i;
                }
                //special case only custom class parameter to fix
                if (pair.getOriginalParams().size() == 1) {
                    for (int k = 0; k < j; k++) {
                        params.put(parameters[k], pair.getOriginalParams().get(original).getTypeAsString());
                    }
                    params.put(parameterNames.get(i), pair.getOriginalParams().get(i).getTypeAsString());
                }
            }
        }
        return params;
    }

    /**
     * Helper function to transform Constructor to List
     */
    private List<String> getParams(CodeAnalysis analysis, Map<String, String> parameterMapping) {
        List<String> parameters = new ArrayList<>();
        for (Iterator<Map.Entry<String, String>> iterator = parameterMapping.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            String type = entry.getValue();
            if (analysis.getMapMethods2Classes().containsKey(type)) {
                if (iterator.hasNext()) { //if multiple elements are left
                    StringBuilder params = new StringBuilder();
                    boolean eqType = true;
                    while (eqType && iterator.hasNext()) {
                        Map.Entry<String, String> nextEntry = iterator.next();
                        if (nextEntry.getValue().equals(type)) {
                            if (!params.toString().equals("")) {
                                params.append(", ");
                            }
                            params.append(nextEntry.getKey());
                        } else {
                            if (params.toString().equals("")) {//1 parameter object
                                parameters.add("new " + type + "("+ entry.getKey() +")");
                            } else {
                                parameters.add("new " + type + "("+ params +")");
                            }
                            eqType = false;
                            parameters.add(nextEntry.getKey());
                        }
                    }
                } else {//if object only contains last element
                    parameters.add("new " + type + "("+ entry.getKey() +")");
                }
            } else {//if parameter is of basic type
                parameters.add(entry.getKey());
            }
        }
        return parameters;
    }

    //from geeks4geeks sorting a hashmap on values
    private static HashMap<String, Double> sortByValue(HashMap<String, Double> hm)  {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort((o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

//    TODO:REMOVE
//    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
//        List<Scenario> scenarios = new NLPFileReader("src/main/resources/nlp_results.json",
//                "src/test/resources/features/vendingMachine.feature")
//                .getScenarios("vendingMachine.feature");
//        DistanceMatcher matcher = new DistanceMatcher(
//                new File("src/main/java/com/vendingmachine"), scenarios);
//    }
}
