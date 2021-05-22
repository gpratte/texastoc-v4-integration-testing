Feature: Randomly seat game players

  Scenario: seat 0 players at 1 table with no table requests
    Given a game has 0 players
    When seating is done with 1 tables each with 10 seats and 0 table requests for table 0
    Then 0 players are seated at table 1
    And table 1 has 10 dead stacks

  Scenario: seat 9 players at table 1 with no table requests
    Given a game has 9 players
    When seating is done with 1 tables each with 9 seats and 0 table requests for table 0
    Then 9 players are seated at table 1
    And table 1 has 0 dead stacks

  Scenario: seat 13 players at 2 tables with no table requests
    Given a game has 13 players
    When seating is done with 2 tables each with 8 seats and 0 table requests for table 0
    Then 7 players are seated at table 1
    Then 6 players are seated at table 2
    And table 1 has 1 dead stacks
    And table 2 has 2 dead stacks

  Scenario: seat 40 players at 4 tables with 2 table requests to move to table 3
    Given a game has 40 players
    When seating is done with 4 tables each with 10 seats and 2 table requests for table 3
    Then the player requests are fulfilled

  Scenario: seat 2 players at 1 tables with 1 table requests to move to table 3
    Given a game has 2 players
    When seating is done with 1 tables each with 10 seats and 1 table requests for table 3
    Then invalid seating request occurs
