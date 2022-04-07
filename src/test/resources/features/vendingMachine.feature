#FOR TESTING ONLY!
@tag
Feature: Vending Machine

  @tag1
  Scenario Outline: Have Product and user have exact amount of money
    Given user want to buy <product>
    When user insert the <money> dollars
    And press the button with the code
    Then The <product> leaves the machine and the stock reduces in 1unit

    Examples:
      | product     | money | status  |
      | "chips"     |  2.00 | success |
      | "chocolate" |  2.50 | Fail    |
      | "cookie"    |  1.75 | Fail    |
      | "candy"     |  1.50 | Fail    |
      | "juice"     |  3.25 | Fail    |
      | "water"     |  1.50 | Fail    |
      | "coke"      |  2.25 | Fail    |
      | "pepsi"     |  2.25 | Fail    |

  @tag2
  Scenario Outline: Have Product and user to receive change
    Given user want to buy <product>
    When user insert the <money> dollars
    And press the button with the code
    Then The <product> leaves the machine and the stock reduces in 1unit
    And VM give <change> back

    Examples:
      | product     | money | change |
      | "chips"     |  2.50 |   0.50 |
      | "chocolate" |  3.50 |   1.00 |
      | "cookie"    |  2.00 |   0.25 |
      | "candy"     |  2.00 |   0.50 |
      | "juice"     |  5.00 |   1.75 |
      | "water"     |  3.00 |   1.50 |
      | "coke"      |  3.00 |   0.75 |
      | "pepsi"     |  2.25 |   0.00 |

  @tag3
  Scenario Outline: User give less money then price
    Given user want to buy <product>
    When user insert the <money> dollars
    And press the button with the code
    Then The VM ask for "still missing this amount: " <missing> dollars

    Examples:
      | product     | money | missing |
      | "chips"     |  1.00 |    1.00 |
      | "chocolate" |  2.00 |    0.50 |
      | "cookie"    |  1.50 |    0.25 |
      | "candy"     |  0.50 |    1.00 |
      | "juice"     |  1.25 |    2.00 |
      | "water"     |  0.75 |    0.75 |
      | "coke"      |  2.00 |    0.25 |
      | "pepsi"     |  1.25 |    1.00 |