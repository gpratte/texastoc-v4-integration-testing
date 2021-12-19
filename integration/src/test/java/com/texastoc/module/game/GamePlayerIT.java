package com.texastoc.module.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.texastoc.module.game.model.GamePlayer;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class GamePlayerIT extends BaseGameIT {

  private Integer gameId;
  private List<GamePlayer> gamePlayers;
  private List<GamePlayer> retrievedGamePlayers;

  @Before
  public void before() {
    // Before each test
    super.before();
    gameId = null;
    gamePlayers = new LinkedList<>();
    retrievedGamePlayers = null;
  }

  /**
   * Add an existing player as a game player with minimal fields set
   */
  @Test
  public void addEmptyExistingPlayer() {
    // Arrange
    aGameIsCreated();
    aPlayerIsAddedWithNothingSet();
    // Act
    theGameIsUpdatedWithThePlayers();
    // Assert
    theGamePlayersAreRetrieved();
    theRetrievedGamePlayersHaveNothingSet();
  }

  /**
   * Add a first time player as a game player with minimal fields set
   */
  @Test
  public void addEmptyFirstTimePlayer() {
    // Arrange
    aGameIsCreated();
    aFirstTimePlayerIsAddedWithNothingSet();
    // Act
    theGameIsUpdatedWithThePlayers();
    // Assert
    theGamePlayersAreRetrieved();
    theRetrievedFirstTimeGamePlayersHaveNothingSet();
  }

  @Test
  public void addNonEmptyExistingPlayer() {
    // Add an existing player as a game player with all fields set
    aGameIsCreated();
    aPlayerIsAddedWithEverythingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theRetrievedGamePlayersHaveEverythingSet();
  }

  /**
   * Add a first time player as a game player with everything set
   */
  @Test
  public void addNonEmptyFirstTimePlayer() {
    aGameIsCreated();
    aFirstTimePlayerIsAddedWithEverythingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theRetrievedFirstTimeGamePlayersHaveEverythingSet();
  }

  /**
   * Add an existing player as a game player with minimal fields set and then update all fields
   */
  @Test
  public void updateGamePlayer() {
    aGameIsCreated();
    aPlayerIsAddedWithNothingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theGamePlayersAreUpdated();
    theGameIsUpdatedWithTheUpdatedPlayers();
    theRetrievedGamePlayersHaveEverythingSet();
  }

  @Test
  public void knockOutGamePlayer() {
    aGameIsCreated();
    aPlayerIsAddedWithNothingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theGamePlayersWithKnockedOut(true);
    theGameIsUpdatedWithTheUpdatedPlayers();
    theRetrievedGamePlayersWithKnockedOut(true);
  }

  @Test
  public void undoKnockedOutGamePlayer() {
    aGameIsCreated();
    aPlayerIsAddedWithEverythingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theGamePlayersWithKnockedOut(false);
    theGameIsUpdatedWithTheUpdatedPlayers();
    theRetrievedGamePlayersWithKnockedOut(false);
  }

  @Test
  public void rebuyGamePlayer() {
    aGameIsCreated();
    aPlayerIsAddedWithNothingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theGamePlayersWithRebuy(true);
    theGameIsUpdatedWithTheUpdatedPlayers();
    theRetrievedGamePlayersWithRebuy(true);
  }

  @Test
  public void undoGamePlayerRebuy() {
    aGameIsCreated();
    aPlayerIsAddedWithEverythingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    theGamePlayersWithRebuy(false);
    theGameIsUpdatedWithTheUpdatedPlayers();
    theRetrievedGamePlayersWithRebuy(false);
  }

  @Test
  public void deleteGamePlayer() {
    aGameIsCreated();
    aPlayerIsAddedWithNothingSet();
    theGameIsUpdatedWithThePlayers();
    theGamePlayersAreRetrieved();
    allGamePlayersAreDeleted();
    theGamePlayersAreRetrieved();
    thereAreNoGamePlayers();
  }

  private void aGameIsCreated() {
    super.aSeasonExists();
    super.theGameStartsNow();
    super.theGameIsCreated();
    gameId = gameCreated.getId();
  }

  private void aPlayerIsAddedWithNothingSet() {
    GamePlayer gamePlayer = GamePlayer.builder()
        .playerId(GUEST_USER_PLAYER_ID)
        .gameId(gameId)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  private void aPlayerIsAddedWithEverythingSet() {
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

  private void aFirstTimePlayerIsAddedWithEverythingSet() {
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

  private void aFirstTimePlayerIsAddedWithNothingSet() {
    GamePlayer gamePlayer = GamePlayer.builder()
        .firstName("first")
        .lastName("last")
        .email("firstlast@example.com")
        .gameId(gameId)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  private void theGameIsUpdatedWithThePlayers() {
    getGame(gameId);
    gameRetrieved.setPlayers(gamePlayers);
    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameRetrieved, token);
  }

  private void theGameIsUpdatedWithTheUpdatedPlayers() {
    getGame(gameId);
    gameRetrieved.setPlayers(retrievedGamePlayers);
    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameRetrieved, token);
  }

  private void allGamePlayersAreDeleted() {
    getGame(gameId);
    String token = login(USER_EMAIL, USER_PASSWORD);
    for (GamePlayer gamePlayer : gameRetrieved.getPlayers()) {
      deletePlayerFromGame(gameRetrieved.getId(), gamePlayer.getId(), token);
    }
  }

  private void theGamePlayersAreRetrieved() {
    getGame(gameId);
    retrievedGamePlayers = gameRetrieved.getPlayers();
  }

  private void theRetrievedGamePlayersHaveNothingSet() {
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

  private void theRetrievedFirstTimeGamePlayersHaveNothingSet() {
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

  private void theRetrievedGamePlayersHaveEverythingSet() {
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

  private void theRetrievedFirstTimeGamePlayersHaveEverythingSet() {
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

  private void theGamePlayersAreUpdated() {
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

  private void theGamePlayersWithKnockedOut(boolean knockedOut) {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      gamePlayer.setKnockedOut(knockedOut);
    }
  }

  private void theRetrievedGamePlayersWithKnockedOut(boolean knockedOut) {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      if (knockedOut) {
        assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
      } else {
        assertFalse("knocked out should be false", gamePlayer.isKnockedOut());
      }
    }
  }

  private void theGamePlayersWithRebuy(boolean knockedOut) {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      gamePlayer.setRebought(knockedOut);
    }
  }

  private void theRetrievedGamePlayersWithRebuy(boolean knockedOut) {
    for (GamePlayer gamePlayer : retrievedGamePlayers) {
      if (knockedOut) {
        assertTrue("rebought should be true", gamePlayer.isRebought());
      } else {
        assertFalse("rebought should be false", gamePlayer.isRebought());
      }
    }
  }

  private void thereAreNoGamePlayers() {
    getGame(gameId);
    assertEquals("number of players should be zero", 0, gameRetrieved.getPlayers().size());
  }
}
