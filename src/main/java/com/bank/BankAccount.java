package com.bank;

public class BankAccount {
    public int balance;

    public BankAccount(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int deposit(int amount) {
        this.balance += amount;
        return this.balance;
    }

    public int withdraw(int amount) {
        this.balance -= amount;
        return this.balance;
    }

//    public static void main(String[] args) {
//        BankAccount account = new BankAccount(10);
//        account.withdraw(10);
//        account.deposit(5);
//        account.getBalance();
//        account.setBalance(100);
//    }
}

