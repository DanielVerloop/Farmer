package com.vendingmachine;

public class Product {
    private String name;
    private double price;

    public Product() {

    }

    public Product(String name) {
        this.name = name;
        setPrice();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
        setPrice();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice() {
        switch (name) {
            case "chips":
                this.price = 2.0;
                break;
            case "chocolate":
                this.price = 2.5;
                break;
            case "cookie":
                this.price = 1.75;
                break;
            case "candy":
                this.price = 1.5;
                break;
            case "juice":
                this.price = 3.25;
                break;
            case "water":
                this.price = 1.5;
                break;
            case "coke":
                this.price = 2.25;
                break;
            case "pepsi":
                this.price = 2.25;
                break;

            default:
                throw new IllegalArgumentException("There is no product with that name");

        }
    }

}