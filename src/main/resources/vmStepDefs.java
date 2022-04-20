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
        product = new Product(product);
    }

    Product product;

    @When("inserts the {double} pounds")
    public void insertsTheMoneyPounds(double money) {
        vendingmachine.setAmount(money);
    }

    @And("presses the button with the code for {string}")
    public void pressesTheButtonWithTheCodeForProduct(String product) {
        vendingmachine.setProduct(product);
    }

    @Then("the stock reduces in 1 unit")
    public void theStockReducesIn1Unit() {
        Assert.assertTrue(vendingmachine.getInventoryQtyForThe() == 1);
    }

    @And("the {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        Assert.assertTrue(vendingmachine.getAmountMissingMessage());
    }

    @Given("there exists a vending machine")
    public void thereExistsAVendingMachine() {
        vendingmachine = new VendingMachine();
    }

    @And("it has 10 {string} in its inventory")
    public void itHas10ProductInItsInventory(String product) {
        product = new Product(product);
    }

    @When("the user inserts the {double} dollars")
    public void theUserInsertsTheMoneyDollars(double money) {
        vendingmachine.removeInventory(money);
    }

    @And("selects the {string}")
    public void selectsTheProduct(String product) {
        vendingmachine.setProduct(product);
    }

    @And("the {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        vendingmachine.setAmount(product);
    }

    @Then("the inventory stock must be 9 units")
    public void theInventoryStockMustBe9Units() {
        Assert.assertTrue(vendingmachine.getInventoryQtyForThe() == 9);
    }

    @And("the vending machine gives {double} back")
    public void theVendingMachineGivesChangeBack(double change) {
        Assert.assertTrue(vendingmachine.getAmount() == change);
    }

    @Given("there exists a vending machine")
    public void thereExistsAVendingMachine() {
        vendingmachine = new VendingMachine();
    }

    @And("it has 10 {string} in its inventory")
    public void itHas10ProductInItsInventory(String product) {
        product = new Product(product);
    }

    @When("the user insert the {double}")
    public void theUserInsertTheMoney(double money) {
        vendingmachine.setAmount(money);
    }

    @And("presses the button with the code for the product")
    public void pressesTheButtonWithTheCodeForTheProduct() {
        vendingmachine.getInventoryQtyForThe();
    }

    @Then("the vending machine asks for {string} dollars")
    public void theVendingMachineAsksForMissingDollars(String missing) {
        Assert.assertTrue(vendingmachine.getAmountMissingMessage().equals(missing));
    }
}
