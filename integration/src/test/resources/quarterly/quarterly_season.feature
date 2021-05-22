Feature: CRUD quarterly seasons
  Create, Retrieve, Update and Delete quarterly seasons

  Scenario: create the quarterly seasons
    Given the season start year encompassing today
    When the current season is created
    Then four quarterly seasons should be created

