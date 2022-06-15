import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class calcStepDefs {

    @Given("I have a calculator")
    public void iHaveACalculator() {
        calculator = new Calculator();
    }

    Calculator calculator;

    @When("I add {int} and {int}")
    public void iAddArg0AndArg1(int arg0, int arg1) {
        calculator.subtract(arg0, arg1);
    }

    @Then("the result should be {int}")
    public void theResultShouldBeArg0(int arg0) {
        Assert.assertTrue(calculator.currentValue() == arg0);
    }

    @When("I subtract {int} from {int}")
    public void iSubtractArg0FromArg1(int arg0, int arg1) {
        calculator.subtract(arg0, arg1);
    }
}
