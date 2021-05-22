Feature: Settings features

  Scenario: Get settings
    A non authenticated call to get the settings
    When the settings are retrieved
    Then the settings are correct

