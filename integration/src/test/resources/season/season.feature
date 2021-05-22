Feature: Create, retrieve, end and open a season

  Scenario: create a season
    Given season starts encompassing today
    When the season is created
    Then the start date should be May first
    And the season costs should be set

  Scenario: create and retrieve a season
    Given a season encompassing today exists
    When the season is retrieved
    Then the retrieved season start date should be May first
    And the retrieved season costs should be set

  Scenario: create and end a season
    Given a season encompassing today exists
    When the season is ended
    And the current season is retrieved
    Then the retrieved season should be ended

  Scenario: create, end and open a season
    Given a season encompassing today exists
    And the season is ended
    When the season is opened
    And the current season is retrieved
    Then the retrieved season should not be ended