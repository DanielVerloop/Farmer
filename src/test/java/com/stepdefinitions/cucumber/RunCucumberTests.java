package com.stepdefinitions.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:features/BankAccount.feature"},
        glue = {"com.stepdefinitions.cucumber"})
public class RunCucumberTests {
}