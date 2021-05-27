package com.texastoc.module.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.texastoc.module.game.model.GamePlayer;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.LinkedList;
import java.util.List;

public class GamePlayerIT extends BaseGameIT {

  private Integer gameId;
  private List<GamePlayer> gamePlayers = new LinkedList<>();
  private List<GamePlayer> retrievedGamePlayers = new LinkedList<>();

  @Before
  public void before() {
    // Before each scenario
    super.before();
    gameId = null;
  }

  @When("^a game is created$")
  public void a_game_is_created() throws Exception {
    super.aSeasonExists();
    super.theGameStartsNow();
    super.theGameIsCreated();
    gameId = gameCreated.getId();
  }

  @And("^a player is added with nothing set$")
  public void aPlayerIsAddedWithNothingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
        .playerId(GUEST_USER_PLAYER_ID)
        .gameId(gameId)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @And("^a player is added with everything set$")
  public void aPlayerIsAddedWithEverythingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
        .playerId(GUEST_USER_PLAYER_ID)
        .gameId(gameId)
        .boughtIn(true)
        .rebought(true)
        .annualTocParticipant(true)
        .quarterlyTocParticipant(true)
        .knockedOut(true)
        .roundUpdates(true)
        .chop(111)
        .place(1)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @And("^a first time player is added with everything set$")
  public void aFirstTimePlayerIsAddedWithEverythingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
        .playerId(GUEST_USER_PLAYER_ID)
        .firstName("first")
        .lastName("last")
        .email("firstlast@example.com")
        .gameId(gameId)
        .boughtIn(true)
        .rebought(true)
        .annualTocParticipant(true)
        .quarterlyTocParticipant(true)
        .knockedOut(true)
        .roundUpdates(true)
        .chop(111)
        .place(1)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @And("^a first time player is added with nothing set$")
  public void aFirstTimePlayerIsAddedWithNothingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
        .firstName("first")
        .lastName("last")
        .email("firstlast@example.com")
        .gameId(gameId)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @When("^the game is updated with the players$")
  public void theGameIsUpdatedWithThePlayers() throws Exception {
    //;;!! super.getCurrentGame();
    gameRetrieved.setPlayers(gamePlayers);
    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameRetrieved, token);
  }

  @When("^the game is updated with the updated players$")
  public void theGameIsUpdatedWithTheUpdatedPlayers() throws Exception {
    //;;!! super.getCurrentGame();
    gameRetrieved.setPlayers(retrievedGamePlayers);
    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameRetrieved, token);
  }

  @When("^all players are deleted$")
  public void allPlayersAreDeleted() throws Exception {
    //;;!! super.getCurrentGame();
    String token = login(USER_EMAIL, USER_PASSWORD);
    for (GamePlayer gamePlayer : gameRetrieved.getPlayers()) {
      deletePlayerFromGame(gameRetrieved.getId(), gamePlayer.getId(), token);
    }
  }

  @And("^the current players are retrieved$")
  public void theCurrentPlayersAreRetrieved() throws Exception {
    //;;!! super.getCurrentGame();
    retrievedGamePlayers = gameRetrieved.getPlayers();
  }

  @Then("^the retrieved game players have nothing set$")
  public void theRetrievedGamePlayersHaveNothingSet() {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      assertFalse("bought-in should be false", gamePlayer.isBoughtIn());
      assertFalse("rebought should be false", gamePlayer.isRebought());
      assertFalse("annual toc participant should be false", gamePlayer.isAnnualTocParticipant());
      assertFalse("quarterly toc participant should be false",
          gamePlayer.isQuarterlyTocParticipant());
      assertFalse("knocked out should be false", gamePlayer.isKnockedOut());
      assertFalse("round updates should be false", gamePlayer.isRoundUpdates());
    }
  }

  @Then("^the retrieved first time game players have nothing set$")
  public void theRetrievedFirstTimeGamePlayersHaveNothingSet() {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      assertFalse("bought-in should be false", gamePlayer.isBoughtIn());
      assertFalse("rebought should be false", gamePlayer.isRebought());
      assertFalse("annual toc participant be false", gamePlayer.isAnnualTocParticipant());
      assertFalse("quarterly toc participant should be false",
          gamePlayer.isQuarterlyTocParticipant());
      assertFalse("knocked out should be false", gamePlayer.isKnockedOut());
      assertFalse("round updates should be false", gamePlayer.isRoundUpdates());
      assertEquals("first name should be first", "first", gamePlayer.getFirstName());
      assertEquals("last name should be last", "last", gamePlayer.getLastName());
      assertEquals("name should be first last", "first last", gamePlayer.getName());
      assertEquals("email should be firstlast@example.com", "firstlast@example.com",
          gamePlayer.getEmail());
    }
  }

  @Then("^the retrieved game players have everything set$")
  public void theRetrievedGamePlayersHaveEverythingSet() {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      assertTrue("bought-in should be true", gamePlayer.isBoughtIn());
      assertTrue("rebought should be true", gamePlayer.isRebought());
      assertTrue("annual toc participant be true", gamePlayer.isAnnualTocParticipant());
      assertTrue("quarterly toc participant should be true",
          gamePlayer.isQuarterlyTocParticipant());
      assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
      assertTrue("round updates should be true", gamePlayer.isRoundUpdates());
      assertEquals("chop should be 111", 111, gamePlayer.getChop().intValue());
      assertEquals("place should be 1", 1, gamePlayer.getPlace().intValue());
    }
  }

  @Then("^the retrieved first time game players have everything set$")
  public void theRetrievedFirstTimeGamePlayersHaveEverythingSet() {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      assertTrue("bought-in should be true", gamePlayer.isBoughtIn());
      assertTrue("rebought should be true", gamePlayer.isRebought());
      assertTrue("annual toc participant be true", gamePlayer.isAnnualTocParticipant());
      assertTrue("quarterly toc participant should be true",
          gamePlayer.isQuarterlyTocParticipant());
      assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
      assertTrue("round updates should be true", gamePlayer.isRoundUpdates());
      assertEquals("chop should be 111", 111, gamePlayer.getChop().intValue());
      assertEquals("place should be 1", 1, gamePlayer.getPlace().intValue());
      assertEquals("last name should be last", "last", gamePlayer.getLastName());
      assertEquals("name should be first last", "first last", gamePlayer.getName());
      assertEquals("email should be firstlast@example.com", "firstlast@example.com",
          gamePlayer.getEmail());
    }
  }

  @And("^the current players are updated$")
  public void theCurrentPlayersAreUpdated() throws Exception {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      gamePlayer.setKnockedOut(true);
      gamePlayer.setAnnualTocParticipant(true);
      gamePlayer.setBoughtIn(true);
      gamePlayer.setAnnualTocParticipant(true);
      gamePlayer.setChop(111);
      gamePlayer.setPlace(1);
      gamePlayer.setQuarterlyTocParticipant(true);
      gamePlayer.setRebought(true);
      gamePlayer.setRoundUpdates(true);
    }
  }

  @And("^the current players with knocked out (true|false)")
  public void theCurrentPlayersWithKnockedOut(boolean knockedOut) throws Exception {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      gamePlayer.setKnockedOut(knockedOut);
    }
  }

  @Then("^the retrieved game players with knocked out (true|false)$")
  public void theRetrievedGamePlayersWithKnockedOut(boolean knockedOut) {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      if (knockedOut) {
        assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
      } else {
        assertFalse("knocked out should be false", gamePlayer.isKnockedOut());
      }
    }
  }

  @And("^the current players with rebuy (true|false)")
  public void theCurrentPlayersWithRebuy(boolean knockedOut) throws Exception {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      gamePlayer.setRebought(knockedOut);
    }
  }

  @Then("^the retrieved game players with rebuy (true|false)$")
  public void theRetrievedGamePlayersWithRebuy(boolean knockedOut) {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      if (knockedOut) {
        assertTrue("rebought should be true", gamePlayer.isRebought());
      } else {
        assertFalse("rebought should be false", gamePlayer.isRebought());
      }
    }
  }

  @Then("^there are no players$")
  public void thereAreNoPlayers() throws Exception {
    //;;!! super.getCurrentGame();
    assertEquals("number of players should be zero", 0, gameRetrieved.getPlayers().size());
  }
}
