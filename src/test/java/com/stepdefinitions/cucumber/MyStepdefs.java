package com.stepdefinitions.cucumber;

import com.bank.BankAccount;

import com.github.javaparser.ast.expr.AssignExpr;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

public class MyStepdefs {
    BankAccount bankAccount;

    @Given("a bank account with initial balance of {int}")
    public void aBankAccountWithInitialBalanceOf(int arg0) {
        bankAccount = new BankAccount(arg0);
    }

    @When("we deposit {int} pounds into the account")
    public void weDepositPoundsIntoTheAccount(int arg0) {
        bankAccount.deposit(arg0);
    }

    @Then("the balance should be {int}")
    public void theBalanceShouldBe(int arg0) {
        Assert.assertEquals(bankAccount.getBalance(), arg0);
    }

    @When("we withdraw {int} pounds from the account")
    public void weWithdrawPoundsFromTheAccount(int arg0) {
        bankAccount.withdraw(arg0);
    }

    @Given("a bank account with balance of {int}")
    public void aBankAccountWithBalanceOf(int arg0) {
        bankAccount = new BankAccount(arg0);
    }

}
