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

    @And("it has {int} {string} in its inventory")
    public void itHasArg0ProductInItsInventory(int arg0, String product) {
        inventoryitem = new InventoryItem(new Product(product), arg0);
        vendingmachine.addItemToInventory(inventoryitem);
    }

    InventoryItem inventoryitem;

    @When("inserts the {double} pounds")
    public void insertsTheMoneyPounds(double money) {
        vendingmachine.setAmount(money);
    }

    @And("presses the button with the code for {string}")
    public void pressesTheButtonWithTheCodeForProduct(String product) {
        vendingmachine.setProduct(product);
    }

    @Then("the stock reduces in {int} unit")
    public void theStockReducesInArg0Unit(int arg0) {
        Assert.assertTrue(vendingmachine.getInventoryQtyForThe() == arg0);
    }

    @And("the {string} leaves the machine")
    public void theProductLeavesTheMachine(String product) {
        Assert.assertTrue(vendingmachine.getAmountMissingMessage());
    }

    @When("the user inserts the {double} dollars")
    public void theUserInsertsTheMoneyDollars(double money) {
        vendingmachine.removeInventory(money);
    }

    @And("selects the {string}")
    public void selectsTheProduct(String product) {
        vendingmachine.setProduct(product);
    }

    @Then("the inventory stock must be {int} units")
    public void theInventoryStockMustBeArg0Units(int arg0) {
        Assert.assertTrue(vendingmachine.getInventoryQtyForThe() == arg0);
    }

    @And("the vending machine gives {double} back")
    public void theVendingMachineGivesChangeBack(double change) {
        Assert.assertTrue(vendingmachine.getAmount() == change);
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
