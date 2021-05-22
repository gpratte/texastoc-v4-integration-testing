Feature: Add players to a game

  Scenario: add empty existing player
    Add an existing player as a game player with minimal fields set
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved game players have nothing set

  Scenario: add existing player with all fields set
    Add an existing player as a game player with all fields set
    Given a game is created
    And a player is added with everything set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved game players have everything set

  Scenario: add empty first time player
    Add a first time player as a game player with minimal fields set
    Given a game is created
    And a first time player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved first time game players have nothing set

  Scenario: add empty first time player with everything set
    Add a first time player as a game player with everything set
    Given a game is created
    And a first time player is added with everything set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved first time game players have everything set

  Scenario: update player
    Add an existing player as a game player with minimal fields set and then update all fields
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    And the current players are updated
    When the game is updated with the updated players
    Then the retrieved game players have everything set

  Scenario: knock out a player
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    And the current players with knocked out true
    When the game is updated with the updated players
    Then the retrieved game players with knocked out true

  Scenario: undo knocked out a player
    Given a game is created
    And a player is added with everything set
    When the game is updated with the players
    And the current players are retrieved
    And the current players with knocked out false
    When the game is updated with the updated players
    Then the retrieved game players with knocked out false

  Scenario: rebuy a player
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    And the current players with rebuy true
    When the game is updated with the updated players
    Then the retrieved game players with rebuy true

  Scenario: undo player rebuy
    Given a game is created
    And a player is added with everything set
    When the game is updated with the players
    And the current players are retrieved
    And the current players with rebuy false
    When the game is updated with the updated players
    Then the retrieved game players with rebuy false

  Scenario: delete a player
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    When all players are deleted
    And the current players are retrieved
    Then there are no players

