import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class BarStepDefs {

    @Given("there is a bar")
    public void thereIsABar() {
        bar = new Bar();
    }

    Bar bar;

    @And("the coffee machine contains {int} beans and {int} milk")
    public void theCoffeeMachineContainsArg0BeansAndArg1Milk(int arg0, int arg1) {
        coffeemachine = new CoffeeMachine(arg0, arg1);
        bar.setCoffeeMachine(coffeemachine);
    }

    CoffeeMachine coffeemachine;

    @When("the client orders a coffee")
    public void theClientOrdersACoffee() {
        bar.orderCoffee();
    }

    @Then("the machine at the bar contains {int} milk")
    public void theMachineAtTheBarContainsArg0Milk(int arg0) {
        Assert.assertTrue(bar.askForSoda() == arg0);
    }

    @And("the machine at the bar contains {int} beans")
    public void theMachineAtTheBarContainsArg0Beans(int arg0) {
        Assert.assertTrue(bar.askForSoda() == arg0);
    }

    @And("a coffee machine with {int} beans and {int} milk")
    public void aCoffeeMachineWithArg0BeansAndArg0Milk(int arg0, int arg1) {
        coffeemachine = new CoffeeMachine(arg0, arg1);
        bar.setCoffeeMachine(coffeemachine);
    }

    @When("the client purchases a cappuccino")
    public void theClientPurchasesACappuccino() {
        bar.orderCappuccino();
    }

    @Then("the client can not order another coffee")
    public void theClientCanNotOrderAnotherCoffee() {
        Assert.assertTrue(bar.askForCoffee() == true);
    }

    @Then("the client can still order another coffee")
    public void theClientCanStillOrderAnotherCoffee() {
        Assert.assertTrue(bar.askForCoffee() == true);
    }

    @And("the bar has a soda machine with {double} of {string}")
    public void theBarHasASodaMachineWithLitersOfSoda(double liters, String soda) {
        sodamachine = new SodaMachine(soda, liters);
        bar.setSodaMachine(sodamachine);
    }

    SodaMachine sodamachine;

    @When("the client purchases a {string}")
    public void theClientPurchasesASoda(String soda) {
        bar.orderSoda(soda);
    }

    @Then("the machine contains {double} liters of {string}")
    public void theMachineContainsAmountLitersOfSoda(Double liters, String soda) {
        Assert.assertTrue(bar.askForSoda(amount).equals(soda));
    }

    @And("the bar has an empty soda machine")
    public void theBarHasAnEmptySodaMachine() {
        sodamachine = new SodaMachine();
        bar.setSodaMachine(sodamachine);
    }

    @And("the bar has an empty coffee machine")
    public void theBarHasAnEmptyCoffeeMachine() {
        coffeemachine = new CoffeeMachine();
        bar.setCoffeeMachine(coffeemachine);
    }

    @When("we add {double} of {string} to the soda machine")
    public void weAddAmountOfSodaToTheSodaMachine(double amount, String soda) {
        bar.fillSodaMachine(soda, amount);
    }

    @And("we add a {int} of coffee beans to the coffee machine")
    public void weAddAVolumeOfCoffeeBeansToTheCoffeeMachine() {
        bar.getCoffeeMachine();
    }

    @Then("the coffee machine contains {int} beans")
    public void theCoffeeMachineContainsVolumeBeans() {
        Assert.assertTrue(bar.askForSoda() == volume);
    }

    @And("the soda machine contains {double} of {string}")
    public void theSodaMachineContainsAmountOfSoda(double amount, String soda) {
        Assert.assertTrue(bar.askForSoda(amount).equals(soda));
    }
}
