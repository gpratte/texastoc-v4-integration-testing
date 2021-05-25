package com.texastoc.module.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.texastoc.module.game.model.Game;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.springframework.web.client.HttpClientErrorException;

public class GameStepdefs extends BaseGameStepdefs {

  @Before
  public void before() {
    // Before each scenario
    super.before();
  }

  @Given("^a season exists$")
  public void aSeasonExists() throws Exception {
    super.aSeasonExists();
  }

  @Given("^the game starts now$")
  public void theGameStartsNow() throws Exception {
    super.theGameStartsNow();
  }

  @Given("^the game supplies need to be moved$")
  public void the_game_supplies_need_to_be_moved() {
    gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(true)
        .build();
  }

  @When("^the game is created$")
  public void theGameIsCreated() throws Exception {
    super.theGameIsCreated();
  }


  @When("^another game is created$")
  public void anotherGameIsCreated() throws Exception {
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

  @When("^the game is finalized$")
  public void the_game_is_finalized() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    finalizeGame(gameCreated.getId(), token);
  }

  @When("^the game is unfinalized$")
  public void the_game_is_unfinalized() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    unfinalizeGame(gameCreated.getId(), token);
  }

  @And("^the retrieved game is updated and retrieved$")
  public void the_retrieved_game_is_updated_and_retrieved() throws Exception {
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

  @When("^the game is created and retrieved$")
  public void the_game_is_created_and_retrieved() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);
    gameRetrieved = getGame(gameCreated.getId(), token);
  }

  @When("^the current game is retrieved$")
  public void getCurrentGame() throws Exception {
    super.getCurrentGame();
  }

  @Then("^the current game is found$")
  public void currentGameExists() throws Exception {
    assertNotNull(gameRetrieved);
  }

  @Then("^the game is normal$")
  public void the_game_is_normal() throws Exception {
    assertNewGame(gameCreated);
  }

  @Then("^the game is not transport required$")
  public void the_game_is_not_double_buy_in_nor_transport_required() throws Exception {
    Assert.assertFalse("transport required should be false", gameCreated.isTransportRequired());
  }

  @Then("^the game transport supplies flag is set$")
  public void the_game_transport_supplies_flag_is_set() throws Exception {
    assertNotNull("game create should not be null", gameCreated);

    // Game setup variables
    assertTrue("transport required should be true", gameCreated.isTransportRequired());
  }

  @Then("^the retrieved game is normal$")
  public void the_retrieved_game_is_normal() throws Exception {
    assertNewGame(gameRetrieved);
  }

  @Then("^the retrieved game has no players or payouts$")
  public void the_retrieved_game_has_no_players() throws Exception {
    gameHasNoPlayersOrPayouts(gameRetrieved);
  }

  @Then("^the retrieved game is finalized$")
  public void the_retrieved_game_is_finalized() throws Exception {
    assertTrue("game should be finalized", gameRetrieved.isFinalized());
  }

  @Then("^the retrieved game is unfinalized$")
  public void the_retrieved_game_is_unfinalized() throws Exception {
    assertFalse("game should be unfinalized", gameRetrieved.isFinalized());
  }

  @Then("^the current game has no players or payouts$")
  public void the_current_game_has_no_players() throws Exception {
    gameHasNoPlayersOrPayouts(gameRetrieved);
  }

  @Then("^the new game is not allowed$")
  public void notAllowed() throws Exception {
    assertNotNull(exception);
    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.SC_CONFLICT);
  }

  public void gameHasNoPlayersOrPayouts(Game game) throws Exception {
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
