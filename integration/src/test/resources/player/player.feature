Feature: Player features

  Scenario: Get player
    Given a new player
    When the player is retrieved
    Then the player matches

  Scenario: Get players
    Given a new player
    Given another new player
    When the players are retrieved
    Then the players match

  Scenario: Update player as admin
    An admin updates another player
    Given a new player
    When the admin updates the player
    And the player is retrieved
    Then the updated player matches

  Scenario: Update another player as non-admin
    A non-admin attempts to update another player
    Given a new player
    When the non-admin updates the player
    Then a forbidden error happens

  Scenario: Delete player as admin
    An admin deletes a player
    Given a new player
    When the admin deletes the player
    And the player is retrieved
    Then a not found error happens

  Scenario: Delete player as non-admin
    A non-admin attempts to delete a player
    Given a new player
    When the non-admin deletes the player
    Then a forbidden error happens

  Scenario: Add role as admin
    An admin adds a role
    Given a new player
    When the admin adds a role
    And the player is retrieved
    Then the player has two roles

  Scenario: Add role as non-admin
    A non-admin attempts to add a role
    Given a new player
    When the non-admin adds a role
    Then a forbidden error happens

  Scenario: Remove role as admin
    An admin adds and then removes a role
    Given a new player
    When the admin adds a role
    And the player is retrieved
    And the admin removes a role
    And the player is retrieved
    Then the player has one role

  Scenario: Remove role as non-admin
    A non-admin attempts to remove a role
    Given a new player
    When the admin adds a role
    And the player is retrieved
    And the non-admin removes a role
    Then a forbidden error happens

