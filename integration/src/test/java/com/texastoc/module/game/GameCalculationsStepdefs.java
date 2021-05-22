package com.texastoc.module.game;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.junit.Assert;

public class GameCalculationsStepdefs extends BaseGameStepdefs {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private Integer gameId;

  @Before
  public void before() {
    // Before each scenario
    super.before();
    gameId = null;
  }

  @When("^a calculated game is created$")
  public void a_game_is_created() throws Exception {
    super.aSeasonExists();
    super.theGameStartsNow();
    super.theGameIsCreated();
    gameId = gameCreated.getId();
  }

  @When("^adding players$")
  public void addPlayers(String json) throws Exception {
    List<GamePlayer> gamePlayers = OBJECT_MAPPER.readValue(
        json, new TypeReference<List<GamePlayer>>() {
        });
    super.getCurrentGame();
    String token = login(USER_EMAIL, USER_PASSWORD);
    for (GamePlayer gp : gamePlayers) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .gameId(gameId)
          .firstName(gp.getFirstName() == null ? "first" : gp.getFirstName())
          .lastName(gp.getLastName() == null ? "last" : gp.getLastName())
          .boughtIn(gp.isBoughtIn())
          .annualTocParticipant(gp.isAnnualTocParticipant())
          .quarterlyTocParticipant(gp.isQuarterlyTocParticipant())
          .rebought(gp.isRebought())
          .place(gp.getPlace())
          .chop(gp.getChop())
          .build();
      addFirstTimePlayerToGame(gamePlayer, token);
    }
  }

  @When("^adding a player$")
  public void addingPlayer(String json) throws Exception {
    super.getCurrentGame();
    String token = login(USER_EMAIL, USER_PASSWORD);
    GamePlayer gamePlayer = OBJECT_MAPPER.readValue(json, GamePlayer.class);
    gamePlayer.setGameId(gameId);
    gamePlayer.setFirstName("first");
    gamePlayer.setLastName("last");
    addFirstTimePlayerToGame(gamePlayer, token);
  }

  @When("^updating a player$")
  public void updatePlayer(String json) throws Exception {
    GamePlayer updateGamePlayerInfo = OBJECT_MAPPER.readValue(json, GamePlayer.class);
    super.getCurrentGame();
    String token = login(USER_EMAIL, USER_PASSWORD);
    for (GamePlayer gp : gameRetrieved.getPlayers()) {
      if (gp.getFirstName().equals(updateGamePlayerInfo.getFirstName()) &&
          gp.getLastName().equals(updateGamePlayerInfo.getLastName())) {
        gp.setBoughtIn(updateGamePlayerInfo.isBoughtIn());
        gp.setAnnualTocParticipant(updateGamePlayerInfo.isAnnualTocParticipant());
        gp.setQuarterlyTocParticipant(updateGamePlayerInfo.isQuarterlyTocParticipant());
        gp.setRebought(updateGamePlayerInfo.isRebought());
        gp.setPlace(updateGamePlayerInfo.getPlace());
        gp.setChop(updateGamePlayerInfo.getChop());
        super.updatePlayerInGame(gp, token);
        break;
      }
    }
  }

  @When("^deleting a player$")
  public void deletePlayer() throws Exception {
    super.getCurrentGame();
    String token = login(USER_EMAIL, USER_PASSWORD);
    GamePlayer gamePlayer = gameRetrieved.getPlayers().get(0);
    super.deletePlayerFromGame(gameRetrieved.getId(), gamePlayer.getId(), token);
  }

  @And("^the current calculated game is retrieved$")
  public void getCurrentGame() throws Exception {
    super.getCurrentGame();
  }

  @Then("^the game calculated is$")
  public void calcualatedGame(String json) throws Exception {
    Game game = OBJECT_MAPPER.readValue(json, Game.class);
    assertGame(game, gameRetrieved);
  }

  private void assertGame(Game expected, Game actual) throws Exception {
    assertEquals(expected.getBuyInCollected(), actual.getBuyInCollected());
    assertEquals(expected.getRebuyAddOnCollected(), actual.getRebuyAddOnCollected());
    assertEquals(expected.getAnnualTocCollected(), actual.getAnnualTocCollected());
    assertEquals(expected.getQuarterlyTocCollected(), actual.getQuarterlyTocCollected());
    assertEquals(expected.getTotalCollected(), actual.getTotalCollected());
    assertEquals(expected.getAnnualTocFromRebuyAddOnCalculated(),
        actual.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals(expected.getRebuyAddOnLessAnnualTocCalculated(),
        actual.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals(expected.getTotalCombinedTocCalculated(), actual.getTotalCombinedTocCalculated());
    assertEquals(expected.getKittyCalculated(), actual.getKittyCalculated());
    assertEquals(expected.getPrizePotCalculated(), actual.getPrizePotCalculated());
    assertEquals(expected.getNumPlayers(), actual.getNumPlayers());
    assertEquals(expected.getNumPaidPlayers(), actual.getNumPaidPlayers());
    assertEquals(expected.isChopped(), actual.isChopped());

    // TODO
    //  Q: how is this field used?
    //  A: set by the the clock service when level greater than 7
    //assertEquals(expected.isCanRebuy(), actual.isCanRebuy());

    assertEquals(expected.getPayouts().size(), actual.getPayouts().size());
    List<GamePayout> expectedPayouts = expected.getPayouts();
    List<GamePayout> actualPayouts = actual.getPayouts();
    for (int i = 0; i < expectedPayouts.size(); i++) {
      GamePayout expectedPayout = expectedPayouts.get(i);
      GamePayout actualPayout = actualPayouts.get(i);
      assertEquals(expectedPayout.getPlace(), actualPayout.getPlace());
      assertEquals(expectedPayout.getAmount(), actualPayout.getAmount());
      if (expectedPayout.getChopAmount() == null) {
        Assert.assertNull(actualPayout.getChopAmount());
      } else {
        assertEquals(expectedPayout.getChopAmount().intValue(),
            actualPayout.getChopAmount().intValue());
      }
    }
  }


}
