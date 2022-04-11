import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class vmStepDefs {

    @Given("The user wants to buy {string}")
    public void theUserWantsToBuyProduct(String product) {
        product = new Product(product);
    }

    Product product;

    @When("The user inserts the {double} pounds")
    public void theUserInsertsTheMoneyPounds(double money) {
        product.getName();
    }

    @Then("The stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
    }

    @And("The user presses the button with the code")
    public void theUserPressesTheButtonWithTheCode() {
        product.getName();
    }

    @And("The {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        product.getName();
    }

    @Given("user wants to buy {string}")
    public void userWantsToBuyProduct(String product) {
        product = new Product(product);
    }

    @When("user inserts the {double} dollars")
    public void userInsertsTheMoneyDollars(double money) {
        product.getName();
    }

    @Then("the stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
    }

    @And("presses the button with the code")
    public void pressesTheButtonWithTheCode() {
        product.getName();
    }

    @And("The {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        product.getName();
    }

    @And("vending machine gives {double} back")
    public void vendingMachineGivesChangeBack(double change) {
        product.getName();
    }

    @Given("user want to buy {string}")
    public void userWantToBuyProduct(String product) {
        product = new Product(product);
    }

    @When("user insert the {double} dollars")
    public void userInsertTheMoneyDollars(double money) {
        product.getName();
    }

    @Then("The vending machine asks for {string} dollars")
    public void theVendingMachineAsksForMissingDollars(String missing) {
        Assert.assertTrue(product.missing);
    }

    @And("press the button with the code")
    public void pressTheButtonWithTheCode() {
        product.getName();
    }
}
