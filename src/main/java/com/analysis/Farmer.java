package com.analysis;

import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.WrongWordspaceTypeException;

import java.io.IOException;

/**
 * Main class for executing Farmer
 */
public class Farmer {

    private void execute() throws WrongWordspaceTypeException, IOException, CorruptConfigFileException {
//        Generator generator = new Generator("vmStepDefs");
//        generator.generate(
//                "vendingMachine.feature",
//                "src/test/resources/features/vendingMachine.feature",
//                "src/main/java/com/vendingMachine"
//        );

//        Generator generator = new Generator("bankStepDefs");
//        generator.generate(
//                "transactions.feature",
//                "src/test/resources/features/BankAccount.feature",
//                "src/main/java/com/bank"
//        );

//        Generator generator = new Generator("calcStepDefs");
//        generator.generate(
//                "calculator.feature",
//                "src/test/resources/features/calculator.feature",
//                "C:/Users/danielv/Documents/Git test projects/cucumber-java-master/src/main/java/com/codingstones/bdd/calculator"
//        );

//        Generator generator = new Generator("vatStepDefs");
//        generator.generate(
//                "vat.feature",
//                "src/test/resources/features/vat.feature",
//                "C:/Users/danielv/Documents/Git test projects/cucumber-java-master/src/main/java/com/codingstones/bdd/vat"
//        );

        Generator generator = new Generator("MovieStepDefs");
        generator.generate(
                "MovieTest.feature",
                "src/test/resources/features/MovieTest.feature",
                "C:/Users/danielv/Documents/Git test projects/cinema-with-cucumber-master/src/test/java/com/gr/cinema/domain"
        );

    }

    public static void main(String[] args) throws WrongWordspaceTypeException, IOException, CorruptConfigFileException {
        new Farmer().execute();
    }
}
