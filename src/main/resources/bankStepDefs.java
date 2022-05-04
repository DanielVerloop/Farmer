import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class bankStepDefs {

    @Given("a bank account with initial balance of {int}")
    public void aBankAccountWithInitialBalanceOfArg0(int arg0) {
        bankaccount = new BankAccount(arg0);
    }

    BankAccount bankaccount;

    @When("we deposit {int} pounds into the account")
    public void weDepositArg0PoundsIntoTheAccount(int arg0) {
        bankaccount.deposit(arg0);
    }

    @Then("the balance should be {int}")
    public void theBalanceShouldBeArg0(int arg0) {
        Assert.assertTrue(bankaccount.balance == arg0);
    }

    @When("we withdraw {int} pounds from the account")
    public void weWithdrawArg0PoundsFromTheAccount(int arg0) {
        bankaccount.withdraw(arg0);
    }

    @Given("a bank account with balance of {int}")
    public void aBankAccountWithBalanceOfArg0(int arg0) {
        bankaccount = new BankAccount(arg0);
    }
}
