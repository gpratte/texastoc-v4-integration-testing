Feature: a finalized game triggers the season to recalculate

  Scenario: calculate a quarterly season with one game
    Given a quarterly season started encompassing today
    And a running quarterly game has players
    """
[
  {
    "firstName":"abe",
    "lastName":"abeson",
    "boughtIn":true,
    "quarterlyTocParticipant":true,
    "place":1,
    "chop":null
  }
]
    """
    When the finalized game triggers the quarterly season to recalculate
    Then the calculated quarterly season is retrieved with 1 games played
    Then the quarterly season calculations should be
    """
{
  "qtocCollected":20,
  "numGames":13,
  "numGamesPlayed":1,
  "finalized":false,
  "players":[
    {
      "name":"abe abeson",
      "place":1,
      "points":30,
      "entries":1
    }
  ],
  "payouts":[
    {
      "place":1,
      "amount":10
    },
    {
      "place":2,
      "amount":6
    },
    {
      "place":3,
      "amount":4
    }
  ]
}
    """

  Scenario: calculate a quarterly season with two games
  Two games with enough players to generate estimated payouts
    Given a quarterly season started encompassing today
    And a running quarterly game has players
    """
[
  {
    "firstName":"abe",
    "lastName":"abeson",
    "boughtIn":true,
    "quarterlyTocParticipant":true,
    "place":1,
    "chop":null
  },
  {
    "firstName":"bob",
    "lastName":"bobson",
    "boughtIn":true,
    "quarterlyTocParticipant":true,
    "place":2,
    "chop":null
  },
  {
    "firstName":"coy",
    "lastName":"coyson",
    "boughtIn":true,
    "quarterlyTocParticipant":true,
    "place":3,
    "chop":null
  }
]
    """
    And the running quarterly game is finalized
    And a running quarterly game has existing players
    """
[
  {
    "name":"abe abeson",
    "place":1
  },
  {
    "name":"bob bobson",
    "place":2
  },
  {
    "name":"coy coyson",
    "place":3
  }
]
    """
    When the finalized game triggers the quarterly season to recalculate
    Then the calculated quarterly season is retrieved with 2 games played
    Then the quarterly season calculations should be
    """
{
  "qtocCollected":120,
  "numGames":13,
  "numGamesPlayed":2,
  "finalized":false,
  "players":[
    {
      "name":"abe abeson",
      "place":1,
      "points":70,
      "entries":2
    },
    {
      "name":"bob bobson",
      "place":2,
      "points":54,
      "entries":2
    },
    {
      "name":"coy coyson",
      "place":3,
      "points":42,
      "entries":2
    }
  ],
  "payouts":[
    {
      "place":1,
      "amount":60
    },
    {
      "place":2,
      "amount":36
    },
    {
      "place":3,
      "amount":24
    }
  ]
}
    """
