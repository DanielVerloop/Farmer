import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class bankStepDefs {

    @Given("the user wants to buy {string} from the vending machine")
    public void theUserWantsToBuyProductFromTheVendingMachine(String product) {
        vendingmachine = new VendingMachine(product);
    }

    VendingMachine vendingmachine;

    @When("The user inserts the {double} pounds")
    public void theUserInsertsTheMoneyPounds(double money) {
        vendingmachine.getProduct();
    }

    @Then("The stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
    }

    @And("The user presses the button with the code")
    public void theUserPressesTheButtonWithTheCode() {
        vendingmachine.getProduct();
    }

    @And("The {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        vendingmachine.getChange();
    }

    @Given("the user wants to buy {string} from the vending machine")
    public void theUserWantsToBuyProductFromTheVendingMachine(String product) {
        vendingmachine = new VendingMachine(product);
    }

    @When("user inserts the {double} dollars")
    public void userInsertsTheMoneyDollars(double money) {
        vendingmachine.getProduct();
    }

    @Then("the stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
    }

    @And("presses the button with the code")
    public void pressesTheButtonWithTheCode() {
        vendingmachine.getProduct();
    }

    @And("The {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        vendingmachine.getChange();
    }

    @And("vending machine gives {double} back")
    public void vendingMachineGivesChangeBack(double change) {
        vendingmachine.getChange();
    }

    @Given("the user wants to buy {string} from the vending machine")
    public void theUserWantsToBuyProductFromTheVendingMachine(String product) {
        vendingmachine = new VendingMachine(product);
    }

    @When("user insert the {double} dollars")
    public void userInsertTheMoneyDollars(double money) {
        vendingmachine.getProduct();
    }

    @Then("The vending machine asks for {string} dollars")
    public void theVendingMachineAsksForMissingDollars(String missing) {
        Assert.assertTrue(vendingmachine.getChange() == missing);
    }

    @And("press the button with the code")
    public void pressTheButtonWithTheCode() {
        vendingmachine.getProduct();
    }
}
