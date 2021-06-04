package com.texastoc.module.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.texastoc.module.game.model.Game;
import io.cucumber.java.Before;
import java.time.LocalDate;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;

public class GameIT extends BaseGameIT {

  @Before
  public void before() {
    // Before each scenario
    super.before();
  }

  @Test
  public void createSimpleGame() throws Exception {
    // Arrange
    aSeasonExists();
    theGameStartsNow();
    // Act
    theGameIsCreated();
    // Assert
    theGameIsNormal();
    theGameIsNotTransportRequired();
  }

  @Test
  public void gameRequiresTransportSupplies() throws Exception {
    aSeasonExists();
    theGameSuppliesNeedToBeMoved();
    theGameIsCreated();
    theGameTransportSuppliesFlagIsSet();
  }

  @Test
  public void createRetrieveSimpleGame() throws Exception {
    aSeasonExists();
    theGameStartsNow();
    theGameIsCreatedAndRetrieved();
    theGameRetrievedIsNormal();
    theGameRetrievedHasNoPlayersOrPayouts();
  }

  @Test
  public void createRetrieveSimpleGameBySeason() throws Exception {
    aSeasonExists();
    theGameStartsNow();
    theGameIsCreated();
    theGameIsRetrievedBySeasonId();
    theGameRetrievedHasNoPlayersOrPayouts();
  }
  @Test
  public void createAndUpdateSimpleGame() throws Exception {
    aSeasonExists();
    theGameStartsNow();
    theGameIsCreatedAndRetrieved();
    theGameRetrievedIsUpdatedAndRetrieved();
    theGameRetrievedIsNormal();
  }

  @Test
  public void createAndFinalizeSimpleGame() throws Exception {
    aSeasonExists();
    theGameStartsNow();
    theGameIsCreated();
    theGameIsFinalized();
    theGameIsRetrieved();
    theGameRetrievedIsFinalized();
  }

  @Test
  public void createFinalizeAndUnfinalizeSimpleGame() throws Exception {
    aSeasonExists();
    theGameStartsNow();
    theGameIsCreated();
    theGameIsRetrieved();
    theGameRetrievedIsUnfinalized();
    theGameIsFinalized();
    theGameIsRetrieved();
    theGameRetrievedIsFinalized();
    theGameIsUnfinalized();
    theGameIsRetrieved();
    theGameRetrievedIsUnfinalized();
  }

  // TODO need to use feature flag to toggle this
  @Ignore
  @Test
  public void cannotCreateGame() throws Exception {
    // Try to create a game when there is a game in progress
    aSeasonExists();
    theGameStartsNow();
    theGameIsCreated();
    anotherGameIsCreated();
    theNewGameIsNotAllowed();
  }

  private void theGameSuppliesNeedToBeMoved() {
    gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(true)
        .build();
  }

  private void anotherGameIsCreated() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    try {
      createGame(Game.builder()
          .date(LocalDate.now().plusDays(1))
          .hostId(1)
          .transportRequired(true)
          .build(), seasonCreated.getId(), token);
    } catch (HttpClientErrorException e) {
      exception = e;
    }
  }

  private void theGameIsFinalized() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    finalizeGame(gameCreated.getId(), token);
  }

  private void theGameIsUnfinalized() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    unfinalizeGame(gameCreated.getId(), token);
  }

  private void theGameRetrievedIsUpdatedAndRetrieved() throws Exception {
    Game gameToUpdate = Game.builder()
        .hostId(gameRetrieved.getHostId())
        .date(gameRetrieved.getDate())
        .transportRequired(true)
        .payoutDelta(1)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameToUpdate, token);
    gameRetrieved = getGame(gameCreated.getId(), token);
  }

  private void theGameIsCreatedAndRetrieved() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);
    gameRetrieved = getGame(gameCreated.getId(), token);
  }

  private void theGameIsRetrievedBySeasonId() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameRetrieved = getGameBySeasonId(gameCreated.getSeasonId(), token);
  }

  private void theGameIsRetrieved() throws Exception {
    super.getGame(gameCreated.getId());
  }

  private void currentGameExists() throws Exception {
    assertNotNull(gameRetrieved);
  }

  private void theGameIsNormal() throws Exception {
    assertNewGame(gameCreated);
  }

  private void theGameIsNotTransportRequired() throws Exception {
    Assert.assertFalse("transport required should be false", gameCreated.isTransportRequired());
  }

  private void theGameTransportSuppliesFlagIsSet() throws Exception {
    assertNotNull("game create should not be null", gameCreated);

    // Game setup variables
    assertTrue("transport required should be true", gameCreated.isTransportRequired());
  }

  private void theGameRetrievedIsNormal() throws Exception {
    assertNewGame(gameRetrieved);
  }

  private void theGameRetrievedHasNoPlayersOrPayouts() throws Exception {
    gameHasNoPlayersOrPayouts(gameRetrieved);
  }

  private void theGameRetrievedIsFinalized() throws Exception {
    assertTrue("game should be finalized", gameRetrieved.isFinalized());
  }

  private void theGameRetrievedIsUnfinalized() throws Exception {
    assertFalse("game should be unfinalized", gameRetrieved.isFinalized());
  }

  private void the_current_game_has_no_players() throws Exception {
    gameHasNoPlayersOrPayouts(gameRetrieved);
  }

  private void theNewGameIsNotAllowed() throws Exception {
    assertNotNull(exception);
    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.SC_CONFLICT);
  }

  private void gameHasNoPlayersOrPayouts(Game game) throws Exception {
    assertNotNull("game players should not be null", game.getPlayers());
    assertEquals("num of game players should be zero", 0, (int) game.getNumPlayers());
    assertEquals("num of game players should be zero", 0, (int) game.getPlayers().size());
    assertNotNull("game payouts should not be null", game.getPayouts());
    assertEquals("num of game payouts should be zero", 0, (int) game.getPayouts().size());
  }

  private void assertNewGame(Game game) throws Exception {
    assertNotNull("game created should not be null", game);
    assertTrue("game id should be greater than 0", game.getId() > 0);
    assertTrue("game season id should be greater than 0", game.getSeasonId() > 0);
    assertTrue("game quarterly season id should be greater than 0", game.getQSeasonId() > 0);
    //Assert.assertEquals("game quarter should be 1", 1, game.getQuarter().getValue());

    assertEquals("game host id should be " + GIL_PRATTE_PLAYER_ID, GIL_PRATTE_PLAYER_ID,
        (int) game.getHostId());
    assertEquals("game host name should be " + GIL_PRATTE_NAME, GIL_PRATTE_NAME,
        game.getHostName());

    // Game setup variables
    assertEquals("kitty cost should come from season", KITTY_PER_GAME, (int) game.getKittyCost());
    assertEquals("buy in cost should come from season", GAME_BUY_IN, (int) game.getBuyInCost());
    assertEquals("re buy cost should come from season", GAME_REBUY, (int) game.getRebuyAddOnCost());
    assertEquals("re buy toc debit cost should come from season", GAME_REBUY_TOC_DEBIT,
        (int) game.getRebuyAddOnTocDebitCost());
    assertEquals("toc cost should come from season", TOC_PER_GAME, (int) game.getAnnualTocCost());
    assertEquals("quarterly toc cost should come from season", QUARTERLY_TOC_PER_GAME,
        (int) game.getQuarterlyTocCost());

    // Game time variables
    assertEquals("game buy in collected should be zero", 0, (int) game.getBuyInCollected());
    assertEquals("game rebuy collected should be zero", 0, (int) game.getRebuyAddOnCollected());
    assertEquals("game annual toc collected should be zero", 0, (int) game.getAnnualTocCollected());
    assertEquals("game quarterly toc collected should be zero", 0,
        (int) game.getQuarterlyTocCollected());
    assertEquals("total collected", 0, game.getTotalCollected());

    assertEquals("no annualTocFromRebuyAddOnCalculated", 0,
        game.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals("no rebuyAddOnLessAnnualTocCalculated", 0,
        game.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals("no totalCombinedTocCalculated", 0, game.getTotalCombinedTocCalculated());
    assertEquals("No kitty calculated", 0, game.getKittyCalculated());
    assertEquals("no prizePotCalculated", 0, game.getPrizePotCalculated());

    Assert.assertFalse("not finalized", game.isFinalized());
    Assert.assertNull("started should be null", game.getStarted());
  }
}
