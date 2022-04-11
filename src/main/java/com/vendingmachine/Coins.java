package com.vendingmachine;

public class Coins {
    private int five, ten, twentyFive, fifty, one, two;

    public Coins(int five, int ten, int twentyFive, int fifty, int one, int two) {
        super();
        this.five = five;
        this.ten = ten;
        this.twentyFive = twentyFive;
        this.fifty = fifty;
        this.one = one;
        this.two = two;
    }

    public Coins leastChangePossible(double totalAmount) {

        int five = 0, ten = 0, twentyFive = 0, fifty = 0, one = 0, two = 0;
        totalAmount = totalAmount * 100;

        while (totalAmount > 0) {
            while (totalAmount % 200 > 0) {
                totalAmount = totalAmount - 200;
                two++;
            }
            while (totalAmount % 100 > 0) {
                totalAmount = totalAmount - 100;
                one++;
            }
            while (totalAmount % 50 > 0) {
                totalAmount = totalAmount - 50;
                fifty++;
            }
            while (totalAmount % 25 > 0) {
                totalAmount = totalAmount - 25;
                twentyFive++;
            }
            while (totalAmount % 10 > 0) {
                totalAmount = totalAmount - 10;
                ten++;
            }
            while (totalAmount % 5 > 0) {
                totalAmount = totalAmount - 5;
                five++;
            }

        }
        Coins change = new Coins(five, ten, twentyFive, fifty, one, two);
        return change;
    }

}