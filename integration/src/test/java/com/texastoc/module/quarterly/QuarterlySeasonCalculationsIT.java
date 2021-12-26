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
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;

public class QuarterlySeasonCalculationsIT extends BaseQuarterlySeasonIT {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static final List<String> GAME_PLAYERS = new LinkedList<>();
  static final List<String> QUARTERLY_SEASON_CALCULATIONS = new LinkedList<>();

  @Before
  public void before() {
    super.before();
  }

  @Test
  public void calculateAQuarterlySeasonWithOneGame() {
    // Arrange
    seasonExists();
    gameHasPlayers(GAME_PLAYERS.get(0));
    // Act
    finalizeGame();
    // Assert
    getCalcuatedQuarterlySeason(1);
    checkQuarterlySeasonCalculations(QUARTERLY_SEASON_CALCULATIONS.get(0));
  }

  @Test
  public void calculateAQuarterlySeasonWithTwoGames() {
    // Two games with enough players to generate estimated payouts
    seasonExists();
    gameHasPlayers(GAME_PLAYERS.get(1));
    finalizeGame();
    gameInProgressAddExistingPlayer(GAME_PLAYERS.get(2));
    finalizeGame();
    getCalcuatedQuarterlySeason(2);
    checkQuarterlySeasonCalculations(QUARTERLY_SEASON_CALCULATIONS.get(1));
  }

  // A quarterly season started encompassing today
  private void seasonExists() {
    aSeasonExists();
  }

  // A running quarterly game has players
  private void gameHasPlayers(String json) {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);

    List<GamePlayer> gamePlayers = null;
    try {
      gamePlayers = OBJECT_MAPPER.readValue(
          json, new TypeReference<List<GamePlayer>>() {
          });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
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

  // A running quarterly game has existing players
  private void gameInProgressAddExistingPlayer(String json) {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);

    List<QuarterlySeasonPlayer> qSeasonPlayers = super
        .getCurrentQuarterlySeason(seasonCreated.getId(), token).getPlayers();

    List<QuarterlySeasonPlayer> qsGamePlayers = null;
    try {
      qsGamePlayers = OBJECT_MAPPER.readValue(
          json, new TypeReference<List<QuarterlySeasonPlayer>>() {
          });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
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

  // The finalized game triggers the quarterly season to recalculate
  private void finalizeGame() {
    String token = login(USER_EMAIL, USER_PASSWORD);
    finalizeGame(gameCreated.getId(), token);
  }

  // The calculated quarterly season is retrieved with (\\d+) games played
  private void getCalcuatedQuarterlySeason(int numGames) {
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

  // The quarterly season calculations should be
  private void checkQuarterlySeasonCalculations(String json) {
    QuarterlySeason expectedSeason = null;
    try {
      expectedSeason = OBJECT_MAPPER.readValue(json, QuarterlySeason.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

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

  static {
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"firstName\":\"abe\","
        + "    \"lastName\":\"abeson\","
        + "    \"boughtIn\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"place\":1,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"firstName\":\"abe\","
        + "    \"lastName\":\"abeson\","
        + "    \"boughtIn\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"place\":1,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"firstName\":\"bob\","
        + "    \"lastName\":\"bobson\","
        + "    \"boughtIn\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"place\":2,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"firstName\":\"coy\","
        + "    \"lastName\":\"coyson\","
        + "    \"boughtIn\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"place\":3,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"name\":\"abe abeson\","
        + "    \"place\":1"
        + "  },"
        + "  {"
        + "    \"name\":\"bob bobson\","
        + "    \"place\":2"
        + "  },"
        + "  {"
        + "    \"name\":\"coy coyson\","
        + "    \"place\":3"
        + "  }"
        + "]");

    QUARTERLY_SEASON_CALCULATIONS.add("{"
        + "  \"qtocCollected\":20,"
        + "  \"numGames\":13,"
        + "  \"numGamesPlayed\":1,"
        + "  \"finalized\":false,"
        + "  \"players\":["
        + "    {"
        + "      \"name\":\"abe abeson\","
        + "      \"place\":1,"
        + "      \"points\":30,"
        + "      \"entries\":1"
        + "    }"
        + "  ],"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":10"
        + "    },"
        + "    {"
        + "      \"place\":2,"
        + "      \"amount\":6"
        + "    },"
        + "    {"
        + "      \"place\":3,"
        + "      \"amount\":4"
        + "    }"
        + "  ]"
        + "}");
    QUARTERLY_SEASON_CALCULATIONS.add("{"
        + "  \"qtocCollected\":120,"
        + "  \"numGames\":13,"
        + "  \"numGamesPlayed\":2,"
        + "  \"finalized\":false,"
        + "  \"players\":["
        + "    {"
        + "      \"name\":\"abe abeson\","
        + "      \"place\":1,"
        + "      \"points\":70,"
        + "      \"entries\":2"
        + "    },"
        + "    {"
        + "      \"name\":\"bob bobson\","
        + "      \"place\":2,"
        + "      \"points\":54,"
        + "      \"entries\":2"
        + "    },"
        + "    {"
        + "      \"name\":\"coy coyson\","
        + "      \"place\":3,"
        + "      \"points\":42,"
        + "      \"entries\":2"
        + "    }"
        + "  ],"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":60"
        + "    },"
        + "    {"
        + "      \"place\":2,"
        + "      \"amount\":36"
        + "    },"
        + "    {"
        + "      \"place\":3,"
        + "      \"amount\":24"
        + "    }"
        + "  ]"
        + "}");
  }

}
