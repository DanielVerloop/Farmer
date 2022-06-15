import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class CoffeeMachineStepDefs {

    @Given("we have a coffee machine with {int} beans and {int} milk")
    public void weHaveACoffeeMachineWithArg0BeansAndArg1Milk(int arg0, int arg1) {
        coffeemachine = new CoffeeMachine(arg0, arg1);
    }

    CoffeeMachine coffeemachine;

    @When("we brew a cup of coffee")
    public void weBrewACupOfCoffee() {
        coffeemachine.makeCoffee();
    }

    @Then("the machine contains {int} beans")
    public void theMachineContainsArg0Beans(int arg0) {
        Assert.assertTrue(coffeemachine.getCoffeeBeans() == arg0);
    }

    @Given("we have a coffee machine without milk or beans")
    public void weHaveACoffeeMachineWithoutMilkOrBeans() {
        coffeemachine = new CoffeeMachine();
    }

    @And("we put {int} beans into the machine")
    public void wePutArg0BeansIntoTheMachine(int arg0) {
        coffeemachine.setCoffeeBeans(arg0);
    }

    @And("we put {int} milk into the machine")
    public void wePutArg0MilkIntoTheMachine(int arg0) {
        coffeemachine.setMilk(arg0);
    }

    @When("we make a cup of coffee")
    public void weMakeACupOfCoffee() {
        coffeemachine.makeCoffee();
    }

    @When("we make a cappuccino")
    public void weMakeACappuccino() {
        coffeemachine.makeCappuccino();
    }

    @And("the machine contains {int} milk")
    public void theMachineContainsArg0Milk(int arg0) {
        Assert.assertTrue(coffeemachine.getMilk() == arg0);
    }
}
