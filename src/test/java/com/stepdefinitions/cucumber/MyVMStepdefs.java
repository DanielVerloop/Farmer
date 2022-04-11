package com.stepdefinitions.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class MyVMStepdefs {
    @Given("user want to buy <product>")
    public void userWantToBuyProduct() {

    }

    @When("user insert the <money> dollars")
    public void userInsertTheMoneyDollars() {

    }

    @And("press the button with the code")
    public void pressTheButtonWithTheCode() {
    }

    @Then("The <product> leaves the machine and the stock reduces in {int}unit")
    public void theProductLeavesTheMachineAndTheStockReducesInUnit(int arg0) {

    }

    @And("VM give <change> back")
    public void vmGiveChangeBack(int change) {

    }

    @Then("The VM ask for {string} <missing> dollars")
    public void theVMAskForMissingDollars(String arg0) {

    }
}
