Feature: a finalized game triggers the season to recalculate

  Scenario: calculate a season with one game
    Given a season started encompassing today
    And a running game has players
    """
[
  {
    "firstName":"abe",
    "lastName":"abeson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":1,
    "chop":null
  }
]
    """
    When the finalized game triggers the season to recalculate
    Then the calculated season is retrieved with 1 games played
    Then the season calculations should be
    """
{
  "buyInCollected":40,
  "rebuyAddOnCollected":40,
  "annualTocCollected":20,
  "totalCollected":120,
  "annualTocFromRebuyAddOnCalculated":20,
  "rebuyAddOnLessAnnualTocCalculated":20,
  "totalCombinedAnnualTocCalculated":40,
  "kittyCalculated":10,
  "prizePotCalculated":50,
  "numGames":52,
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
  "payouts":[],
  "estimatedPayouts":[]
}
    """

  Scenario: calculate a season with two games
  Two games with enough players to generate estimated payouts
    Given a season started encompassing today
    And a running game has players
    """
[
  {
    "firstName":"abe",
    "lastName":"abeson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":1,
    "chop":null
  },
  {
    "firstName":"bob",
    "lastName":"bobson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":2,
    "chop":null
  },
  {
    "firstName":"coy",
    "lastName":"coyson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":3,
    "chop":null
  }
]
    """
    And the running game is finalized
    And a running game has existing players
    """
[
  {
    "firstName":"abe",
    "lastName":"abeson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":1,
    "chop":null
  },
  {
    "firstName":"bob",
    "lastName":"bobson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":2,
    "chop":null
  },
  {
    "firstName":"coy",
    "lastName":"coyson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":3,
    "chop":null
  }
]
    """
    When the finalized game triggers the season to recalculate
    Then the calculated season is retrieved with 2 games played
    Then the season calculations should be
    """
{
  "buyInCollected":240,
  "rebuyAddOnCollected":240,
  "annualTocCollected":120,
  "totalCollected":720,
  "annualTocFromRebuyAddOnCalculated":120,
  "rebuyAddOnLessAnnualTocCalculated":120,
  "totalCombinedAnnualTocCalculated":240,
  "kittyCalculated":20,
  "prizePotCalculated":340,
  "numGames":52,
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
  "payouts":[],
  "estimatedPayouts":[
    {
      "place":1,
      "amount":1649,
      "guaranteed":true,
      "estimated":true,
      "cash":false
    },
    {
      "place":2,
      "amount":1598,
      "guaranteed":false,
      "estimated":true,
      "cash":false
    },
    {
      "place":3,
      "amount":1348,
      "guaranteed":false,
      "estimated":true,
      "cash":false
    },
    {
      "place":4,
      "amount":1273,
      "guaranteed":false,
      "estimated":true,
      "cash":false
    },
    {
      "place":5,
      "amount":372,
      "guaranteed":false,
      "estimated":true,
      "cash":true
    }
  ]
}
    """
