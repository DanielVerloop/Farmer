package com.analysis;

import com.analysis.structures.Context;
import com.analysis.structures.Rule;
import com.analysis.structures.Scenario;
import com.analysis.structures.constructor.Constructor;
import com.analysis.structures.constructor.ConstructorPair;
import com.analysis.structures.steps.*;
import com.analysis.util.Advice;
import com.analysis.util.ParameterParser;
import com.analysis.util.ParameterTester;
import com.analysis.util.StringFormatter;
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
    private final DiscoStringSimilarity model = new DiscoStringSimilarity();
    private final Cosine cosine = new Cosine();//All other text-code matching
    private final NormalizedLevenshtein levenshtein = new NormalizedLevenshtein();//Used for class matching
    private final ParameterParser parameterParser = new ParameterParser(new File("src/test/resources/features/vendingMachine.feature"));//TODO: move instantiation to constructor!
    private final List<Scenario> match;

    @Override
    public List<Scenario> getMatch() {
        return match;
    }

    public DistanceMatcher(File targetDir, List<Scenario> scenarios) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        CodeAnalysis analysis = new CodeAnalysis(targetDir);
        match = this.match(scenarios, analysis);
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
        String bestMatchedClass = constructorResolver.getClassName();

        //check if we need to use method calls or object instantiations to couple the and step to given step.
        if (context.getMainClass().equals(bestMatchedClass)) {//method calls only
            String matchedMethod = matchMethod(step.getSrlLabels(), step.getVerbs(), step, analysis, bestMatchedClass);
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
            List<String> parameters = getParams(analysis, constructorResolver);
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
        String bestMatchedClass = constructorResolver.getClassName();

        //Get the matched parameter variables
        if (constructorResolver.getParameters().isEmpty()) {//no param-constructor
            return Arrays.asList(new Rule(Advice.OBJECTI, bestMatchedClass, null));
        }
        // we were able to match parameters with a constructor
        //Resolve mapping to correct parameter types
        List<String> parameters = getParams(analysis, constructorResolver);

        return Arrays.asList(new Rule(Advice.OBJECTI, bestMatchedClass, parameters, "paramsUsed"));
    }

    private List<Rule> matchWhen(WhenStep step, CodeAnalysis analysis, Context context) throws IOException, WrongWordspaceTypeException {
        //TODO: for each verb analyse using srl
        // use ARG0, ARG1 and ARG2 to see what method is supposed to do
        // then use this to suggest method calls
        //Get class from given step
         String matchedClass = context.getMainClass();//get class from given-step
        //get the methods of the most common class
        List<String> methods = analysis.getMapMethods2Classes().get(matchedClass);
        //find the closest method
        String matchedMethod = matchMethod(step.getSrlLabels(), step.getVerbs(), step, analysis, matchedClass);

        //Add parameters
        //TODO: check ordering of parameters!
        List<String> param = new ArrayList<>();
        if (step.getParameters().size() > 0){
            for (String parameter : step.getParameters()) {
                param.add(parameter);
            }
        }
        //TODO:find more organized way of transforming numbers to parameters
        if (step.getNumbers().size() > 0) { // numbers are present
            for (int i = 0; i < step.getNumbers().size(); i++) {
                param.add("arg"+i);
            }
        }

        return Arrays.asList(new Rule(Advice.METHODI,matchedClass, param, matchedMethod));
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
        String fieldName = matchVar(analysis, step.getNouns());
        String methodName = matchGet(step.getSrlLabels(), step.getVerbs(), step, analysis, matchedClass);

        if (step.getNumbers().size() == 1) {
            //highly likely a number parameter
            //if arg2 refers to the number then this is highly likely used in assert statement
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
                methodName = matchGet(step.getSrlLabels(), step.getVerbs(), step, analysis, matchedClass);
                if (Arrays.asList("double", "int").contains(step.getParent().getTypeSolver().getParameterType(compareValue))) {
                    assertStmt = "equals";
                } else {//string comparison
                    assertStmt = "sequals";
                }
            }
        }
        if (fieldName != null && fieldName != "") {
            return Arrays.asList(new Rule(Advice.ASSERT, context.getMainClass(), fieldName, compareValue, assertStmt));
        }
        return Arrays.asList(new Rule(Advice.ASSERT, context.getMainClass(), methodName, parameters, compareValue, assertStmt));
    }

    /**
     * Match a method in the context of assert statement
     * @param advice
     * @param verbs
     * @param step
     * @param analysis
     * @param className
     * @return matched method name
     */
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
        //TODO: returnNumberType on bigger target classes
        classVariables.forEach((s, vars) -> {
            for (String noun : nouns) {
                double smallestDist = Integer.MAX_VALUE;
                String matchedVar = "";
                for (String var: vars) {
                    double dist = this.levenshtein.distance(var, noun);
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

    /**
     * Used to find a method that uses a second class as parameter
     * @param srlLabels
     * @param verbs
     * @param step
     * @param analysis
     * @param matchedClass
     * @param newClass
     * @return
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

    private String matchMethod(Map<String, String> advice, Set<String> verbs, Step step, CodeAnalysis analysis, String matchedClass) throws IOException, WrongWordspaceTypeException {
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
     * @param n
     * @return n best classname matches
     */
    private List<String> matchClass(List<String> nouns, CodeAnalysis codeAnalysis, int n) {
        HashMap<String, Double> similarity = new HashMap<>(); // {word=[suggested class]}
        for (String noun : nouns) {
            double smallestDist = Integer.MAX_VALUE;
            String matchedClass = "";
            for (String className : codeAnalysis.getMapMethods2Classes().keySet()) {
                double dist = this.cosine.distance(className, noun) +
                        this.levenshtein.distance(className, noun);
                similarity.merge(className, dist, Double::sum);
            }

        }
        HashMap<String, Double> sortedOnSim = sortByValue(similarity);
        List<String> result = new ArrayList<>();
        List<String> setIterable = new ArrayList<>();
        setIterable.addAll(sortedOnSim.keySet());
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
        List<String> parameterNames = new ArrayList<>();
        //list of types we have to match
        List<String> targetTypes = new ArrayList<>();
        for (String param : step.getParameters()) { //use the ordering to remember names
            targetTypes.add(parameterParser.getParameterType(param));
        }
        parameterNames.addAll(step.getParameters());
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
            for (ConstructorPair resolvedConstructor : analysis.constructorParamResolver(className)) {
                List<Parameter> baseTypeConstructor = resolvedConstructor.getBaseParams();
                if (analysis.checkParamTypes(baseTypeConstructor, targetTypes) &&
                    targetTypes.size() == baseTypeConstructor.size()) {//correct param types
                    String[] parameters = new String[targetTypes.size()];

                    //loop over all possible combination and match most likely param
                    for (int i = 0; i < targetTypes.size(); i++) {
                        double dist = Double.MAX_VALUE;
                        for (int j = 0; j < baseTypeConstructor.size(); j++) {
                            if (baseTypeConstructor.get(j).getTypeAsString().equals(targetTypes.get(i))) {
                                double stringDist = this.levenshtein.distance(
                                        baseTypeConstructor.get(j).getNameAsString(), parameterNames.get(i));
                                if (stringDist < dist) {
                                    parameters[j] = parameterNames.get(i);
                                    dist = stringDist;
                                }
                            }
                        }
                    }
                    //TODO:test if code below behaves correctly with more complex cases
                    int original = 0;
                    HashMap<String, String> params = new HashMap<>();
                    for (int i = 0; i < resolvedConstructor.getOriginalParams().size(); i++) {
                        for (int j = i; j < resolvedConstructor.getBaseParams().size(); j++) {
                            if (resolvedConstructor.getBaseParams().get(i).equals(resolvedConstructor.getOriginalParams().get(j))) {
                                for (int k = original; k < j; k++) {
                                    params.put(parameters[k], resolvedConstructor.getOriginalParams().get(original).getTypeAsString());
                                }
                                params.put(parameterNames.get(i), resolvedConstructor.getOriginalParams().get(i).getTypeAsString());
                                original = i;
                            }
                        }
                    }
                    return new Constructor(className, params);
                }
            }
        }
        return new Constructor(bestMatchedClasses.get(0), new HashMap<>());
    }

    /**
     * Helper function to transform Constructor to List
     * @param analysis
     * @param constructorResolver
     * @return
     */
    private List<String> getParams(CodeAnalysis analysis, Constructor constructorResolver) {
        List<String> parameters = new ArrayList<>();
        for (Iterator<Map.Entry<String, String>> iterator = constructorResolver.getParameters().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            String type = entry.getValue();
            if (analysis.getMapMethods2Classes().containsKey(type)) {
                if (iterator.hasNext()) { //if multiple elements are left
                    String params = "";
                    boolean eqType = true;
                    while (eqType && iterator.hasNext()) {
                        Map.Entry<String, String> nextEntry = iterator.next();
                        if (nextEntry.getValue().equals(type)) {
                            if (!params.equals("")) {
                                params += ", ";
                            }
                            params += nextEntry.getKey();
                        } else {
                            if (params.equals("")) {//1 parameter object
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
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        List<Scenario> scenarios = new NLPFileReader("src/main/resources/nlp_results.json",
                "src/test/resources/features/vendingMachine.feature")
                .getScenarios("vendingMachine.feature");
        DistanceMatcher matcher = new DistanceMatcher(
                new File("src/main/java/com/vendingmachine"), scenarios);
    }
}
