import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class SodaMachineStepDefs {

    @Given("we have a soda machine with {double} of {string}")
    public void weHaveASodaMachineWithAmountOfSoda(double amount, String soda) {
        sodamachine = new SodaMachine(soda, amount);
    }

    SodaMachine sodamachine;

    @When("we serve a {string} to a client")
    public void weServeASodaToAClient(String soda) {
        sodamachine.serveDrink(soda);
    }

    @Then("the machine contains {int} of {string}")
    public void theMachineContainsLitersOfSoda(int liters, String soda) {
        Assert.assertTrue(sodamachine.hashCode(liters).equals(soda));
    }

    @When("we add {double} of {string} to the machine")
    public void weAddAmountOfSodaToTheMachine(double amount, String soda) {
        sodamachine.inputSoda(soda, amount);
    }

    @Given("we have an empty soda machine")
    public void weHaveAnEmptySodaMachine() {
        sodamachine = new SodaMachine();
    }

    @And("we serve a {string} to a client")
    public void weServeASodaToAClient(String soda) {
        sodamachine.serveDrink(soda);
    }
}
