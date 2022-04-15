import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class vmStepDefs {

    @Given("there exists a vending machine")
    public void thereExistsAVendingMachine() {
        vendingmachine = new VendingMachine();
    }

    VendingMachine vendingmachine;

    @And("it has 10 {string} in its inventory")
    public void itHas10ProductInItsInventory(String product) {
        vendingmachine = new VendingMachine();
    }

    @When("inserts the {double} pounds")
    public void insertsTheMoneyPounds(double money) {
        vendingmachine.setAmount(money);
    }

    @And("presses the button with the code for {string}")
    public void pressesTheButtonWithTheCodeForProduct(String product) {
        vendingmachine.setAmount(money);
    }

    @Then("the stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
        Assert.assertTrue(vendingmachine.getPrice() == 1);
    }

    @And("the {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        Assert.assertTrue(vendingmachine.getPrice() == 1);
    }

    @Given("there exists a vending machine")
    public void thereExistsAVendingMachine() {
        vendingmachine = new VendingMachine();
    }

    @And("it has 10 {string} in its inventory")
    public void itHas10ProductInItsInventory(String product) {
        vendingmachine = new VendingMachine();
    }

    @When("user inserts the {double} dollars")
    public void userInsertsTheMoneyDollars(double money) {
        vendingmachine.setAmount(money);
    }

    @And("presses the button with the code for the product")
    public void pressesTheButtonWithTheCodeForTheProduct() {
        vendingmachine.setAmount(money);
    }

    @And("the {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        vendingmachine.setAmount(money);
    }

    @Then("the stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
        Assert.assertTrue(vendingmachine.getPrice() == 1);
    }

    @And("vending machine gives {double} back")
    public void vendingMachineGivesChangeBack(double change) {
        Assert.assertTrue(vendingmachine.getPrice() == 1);
    }

    @Given("there exists a vending machine")
    public void thereExistsAVendingMachine() {
        vendingmachine = new VendingMachine();
    }

    @And("it has 10 {string} in its inventory")
    public void itHas10ProductInItsInventory(String product) {
        vendingmachine = new VendingMachine();
    }

    @When("the user insert the {double} dollars")
    public void theUserInsertTheMoneyDollars(double money) {
        vendingmachine.setAmount(money);
    }

    @And("presses the button with the code for the product")
    public void pressesTheButtonWithTheCodeForTheProduct() {
        vendingmachine.setAmount(money);
    }

    @Then("the vending machine asks for {string} dollars")
    public void theVendingMachineAsksForMissingDollars(String missing) {
        Assert.assertTrue(vendingmachine.getPrice().equals(missing));
    }
}
