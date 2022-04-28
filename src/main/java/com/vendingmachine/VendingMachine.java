package com.vendingmachine;

import java.util.List;
import java.util.ArrayList;

public class VendingMachine {
    private Product product;
    private double amount;
    private List<InventoryItem> inventory = new ArrayList<>();

    public VendingMachine() {

    }

    public VendingMachine(List<InventoryItem> inventory) {
        this.inventory = inventory;
    }

    public int getInventoryQtyForThe(String product) {
        int qty = 0;
        for (InventoryItem inventoryItem : inventory) {
            if (inventoryItem.getProduct().getName().equals(product))
                qty = inventoryItem.getQty();
        }
        return qty;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean checkForChange() {
        return (amount > product.getPrice());
    }

    public Double getChange() {
        if (checkForChange())
            return amount - product.getPrice();
        return 0.0;
    }

    public String getAmountMissingMessage() {
        double amountMissing = 0;
        if (amount < product.getPrice()) {

            amountMissing = product.getPrice() - amount;
            return "still missing this amount: " + amountMissing;
        } else {
            return null;
        }
    }

    public void addProductToInventory(Product product, int quantity) {
        InventoryItem item = new InventoryItem(product, quantity);
        this.inventory.add(item);
    }

    public void removeInventory(String product) {
        for (InventoryItem inventoryItem : inventory) {
            if (inventoryItem.getProduct().getName().equals(product))
                inventoryItem.setQty(inventoryItem.getQty() - 1);
        }
    }
}