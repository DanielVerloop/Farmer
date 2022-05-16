Feature: VAT Calculation

  @ok
  Scenario Outline: Spanish no-general VAT
    Given I have a VAT calculator
    When tax is calculated for "<product>" with <price> price
    Then the result should be <result>

    Examples:
      | product | price | result |
      | bread   | 100   | 104    |
      | diapers | 100   | 121    |
      | wine    | 100   | 110    |
      | panizo  | 100   | -1     |