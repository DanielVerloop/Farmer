Feature: Transactions
  In order to manage my money,
  As an account owner
  I want to deposit and withdraw money from my account
  So that I can use it

  Scenario Outline: deposit money to empty account
    Given a bank account with initial balance of 0
    And the bank account is from RaboBank
    And is not illegal
    When we deposit 100 pounds into the account
    Then the balance should be 100
    Examples:

  Scenario: withdraw money from a bank account
    Given a bank account with initial balance of 1000
    When we withdraw 100 pounds from the account
    Then the balance should be 900

  Scenario: deposit and withdraw money from a bank account
    Given a bank account with balance of 100
    When we deposit 20 pounds into the account
    Then the balance should be 120