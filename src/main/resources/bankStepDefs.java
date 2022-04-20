import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class bankStepDefs {

    @Given("a bank account with initial balance of 0")
    public void aBankAccountWithInitialBalanceOf0() {
        bankaccount = new BankAccount(0);
    }

    BankAccount bankaccount;

    @When("we deposit 100 pounds into the account")
    public void weDeposit100PoundsIntoTheAccount() {
        bankaccount.deposit(100);
    }

    @Then("the balance should be 100")
    public void theBalanceShouldBe100() {
        Assert.assertTrue(bankaccount.balance == 100);
    }

    @Given("a bank account with initial balance of 1000")
    public void aBankAccountWithInitialBalanceOf1000() {
        bankaccount = new BankAccount(1000);
    }

    @When("we withdraw 100 pounds from the account")
    public void weWithdraw100PoundsFromTheAccount() {
        bankaccount.withdraw(100);
    }

    @Then("the balance should be 900")
    public void theBalanceShouldBe900() {
        Assert.assertTrue(bankaccount.balance == 900);
    }

    @Given("a bank account with balance of 100")
    public void aBankAccountWithBalanceOf100() {
        bankaccount = new BankAccount(100);
    }

    @When("we deposit 20 pounds into the account")
    public void weDeposit20PoundsIntoTheAccount() {
        bankaccount.deposit(20);
    }

    @Then("the balance should be 120")
    public void theBalanceShouldBe120() {
        Assert.assertTrue(bankaccount.balance == 120);
    }
}