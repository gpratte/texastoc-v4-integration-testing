package com.texastoc.module.quarterly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.model.QuarterlySeasonPlayer;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;

public class QuarterlySeasonCalculationsStepdefs extends BaseQuarterlySeasonStepdefs {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Before
  public void before() {
    super.before();
  }

  @Given("^a quarterly season started encompassing today$")
  public void seasonExists() throws Exception {
    aSeasonExists();
  }

  @And("^a running quarterly game has players$")
  public void gameInProgress(String json) throws Exception {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, token);

    List<GamePlayer> gamePlayers = OBJECT_MAPPER.readValue(
        json, new TypeReference<List<GamePlayer>>() {
        });
    for (GamePlayer gp : gamePlayers) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .gameId(gameCreated.getId())
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

  @And("^a running quarterly game has existing players$")
  public void gameInProgressAddExistingPlayer(String json) throws Exception {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, token);

    List<QuarterlySeasonPlayer> qSeasonPlayers = super
        .getCurrentQuarterlySeason(seasonCreated.getId(), token).getPlayers();

    List<QuarterlySeasonPlayer> qsGamePlayers = OBJECT_MAPPER.readValue(
        json, new TypeReference<List<QuarterlySeasonPlayer>>() {
        });
    for (QuarterlySeasonPlayer gp : qsGamePlayers) {
      QuarterlySeasonPlayer qSeasonPlayer = qSeasonPlayers.stream()
          .filter(qsp -> qsp.getName().equals(gp.getName()))
          .findFirst().get();
      GamePlayer gamePlayer = GamePlayer.builder()
          .gameId(gameCreated.getId())
          .playerId(qSeasonPlayer.getPlayerId())
          .boughtIn(true)
          .quarterlyTocParticipant(true)
          .place(gp.getPlace())
          .build();
      addPlayerToGame(gamePlayer, token);
    }
  }

  @And("^the running quarterly game is finalized$")
  public void finalizedGame() throws JsonProcessingException {
    String token = login(USER_EMAIL, USER_PASSWORD);
    super.finalizeGame(gameCreated.getId(), token);
  }

  @When("^the finalized game triggers the quarterly season to recalculate$")
  public void finalizeGame() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    finalizeGame(gameCreated.getId(), token);
  }

  @Given("^the calculated quarterly season is retrieved with (\\d+) games played$")
  public void getCalcuatedQuarterlySeason(int numGames) throws Exception {
    final String token = login(USER_EMAIL, USER_PASSWORD);
    Awaitility.await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          List<QuarterlySeason> qSeasons = super.getQuarterlySeasons(seasonCreated.getId(), token);
          qSeasonRetrieved = getCurrentQuarterlySeason(qSeasons);
          Assertions.assertThat(qSeasonRetrieved.getNumGamesPlayed()).isEqualTo(numGames);
        });
  }

  @Then("^the quarterly season calculations should be$")
  public void checkSeasonCalculations(String json) throws Exception {
    QuarterlySeason expectedSeason = OBJECT_MAPPER.readValue(json, QuarterlySeason.class);

    assertEquals(expectedSeason.getQTocCollected(), qSeasonRetrieved.getQTocCollected());
    assertEquals(expectedSeason.getNumGames(), qSeasonRetrieved.getNumGames());
    assertEquals(expectedSeason.getNumGamesPlayed(), qSeasonRetrieved.getNumGamesPlayed());

    assertEquals(expectedSeason.getPlayers().size(), qSeasonRetrieved.getPlayers().size());
    for (int i = 0; i < expectedSeason.getPlayers().size(); i++) {
      assertEquals(qSeasonRetrieved.getSeasonId(),
          qSeasonRetrieved.getPlayers().get(i).getSeasonId());
      assertEquals(qSeasonRetrieved.getId(), qSeasonRetrieved.getPlayers().get(i).getQSeasonId());
      assertEquals(expectedSeason.getPlayers().get(i).getName(),
          qSeasonRetrieved.getPlayers().get(i).getName());
      if (expectedSeason.getPlayers().get(i).getPlace() == null) {
        assertNull(qSeasonRetrieved.getPlayers().get(i).getPlace());
      } else {
        assertEquals(expectedSeason.getPlayers().get(i).getPlace(),
            qSeasonRetrieved.getPlayers().get(i).getPlace());
      }
      assertEquals(expectedSeason.getPlayers().get(i).getPoints(),
          qSeasonRetrieved.getPlayers().get(i).getPoints());
      assertEquals(expectedSeason.getPlayers().get(i).getEntries(),
          qSeasonRetrieved.getPlayers().get(i).getEntries());
    }

    assertEquals(expectedSeason.getPayouts().size(), qSeasonRetrieved.getPayouts().size());
    for (int i = 0; i < expectedSeason.getPayouts().size(); i++) {
      assertEquals(qSeasonRetrieved.getId(), qSeasonRetrieved.getPayouts().get(i).getQSeasonId());
      assertEquals(qSeasonRetrieved.getSeasonId(),
          qSeasonRetrieved.getPayouts().get(i).getSeasonId());
      assertEquals(expectedSeason.getPayouts().get(i).getPlace(),
          qSeasonRetrieved.getPayouts().get(i).getPlace());
      assertEquals(expectedSeason.getPayouts().get(i).getAmount(),
          qSeasonRetrieved.getPayouts().get(i).getAmount());
    }
  }
}
