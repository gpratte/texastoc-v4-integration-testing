package com.texastoc.module.game;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GameCalculationsIT extends BaseGameIT {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private Integer gameId;

  static final List<String> GAME_PLAYERS = new LinkedList<>();
  static final List<String> GAME_CALCULATIONS = new LinkedList<>();

  @Before
  public void before() {
    // Before each scenario
    super.before();
    gameId = null;
  }

  /**
   * A game with one (empty) player
   */
  @Test
  public void gameWithOneEmptyPlayer() throws Exception {
    // Arrange
    aGameIsCreated();
    // Act
    addGamePlayers(GAME_PLAYERS.get(0));
    // Assert
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(0));
  }

  /**
   * A game with one player with everything set all. The player is bought-in, rebought, annual toc
   * participant, quarterly toc participant and is in first place.
   */
  @Test
  public void gameWithOnePlayer() throws Exception {
    aGameIsCreated();
    addGamePlayers(GAME_PLAYERS.get(1));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(1));
  }

  /**
   * A game with two players and then update a player
   */
  @Test
  public void gameWithTwoPlayers() throws Exception {
    aGameIsCreated();
    addGamePlayers(GAME_PLAYERS.get(2));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(2));
    updatePlayer(GAME_PLAYERS.get(3));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(3));
  }

  /**
   * A game with 7 players and then add a player will result in another payout
   */
  @Test
  public void gameWithSevenPlayers() throws Exception {
    aGameIsCreated();
    addGamePlayers(GAME_PLAYERS.get(4));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(4));
    addingPlayer(GAME_PLAYERS.get(5));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(5));
  }

  /**
   * A game with 8 players and delete a player will result in one less payout
   */
  @Test
  public void gameWithEightPlayers() throws Exception {
    aGameIsCreated();
    addGamePlayers(GAME_PLAYERS.get(6));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(6));
    deleteGamePlayer();
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(7));
  }

  /**
   * A game over with ten players with all but one bought-in, 7 have rebought, 6 are annual toc
   * participants, 3 are quarterly toc participants and top 2 chopped the pot
   */
  @Test
  public void gameWithTenPlayers() throws Exception {
    aGameIsCreated();
    addGamePlayers(GAME_PLAYERS.get(7));
    getCalculatedGame();
    calcualatedGame(GAME_CALCULATIONS.get(8));
  }

  private void aGameIsCreated() throws Exception {
    super.aSeasonExists();
    super.theGameStartsNow();
    super.theGameIsCreated();
    gameId = gameCreated.getId();
  }

  private void addGamePlayers(String json) throws Exception {
    List<GamePlayer> gamePlayers = OBJECT_MAPPER.readValue(
        json, new TypeReference<List<GamePlayer>>() {
        });
    getGame(gameId);
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

  private void addingPlayer(String json) throws Exception {
    getGame(gameId);
    String token = login(USER_EMAIL, USER_PASSWORD);
    GamePlayer gamePlayer = OBJECT_MAPPER.readValue(json, GamePlayer.class);
    gamePlayer.setGameId(gameId);
    gamePlayer.setFirstName("first");
    gamePlayer.setLastName("last");
    addFirstTimePlayerToGame(gamePlayer, token);
  }

  private void updatePlayer(String json) throws Exception {
    GamePlayer updateGamePlayerInfo = OBJECT_MAPPER.readValue(json, GamePlayer.class);
    getGame(gameId);
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

  private void deleteGamePlayer() throws Exception {
    getGame(gameId);
    String token = login(USER_EMAIL, USER_PASSWORD);
    GamePlayer gamePlayer = gameRetrieved.getPlayers().get(0);
    super.deletePlayerFromGame(gameRetrieved.getId(), gamePlayer.getId(), token);
  }

  private void getCalculatedGame() throws Exception {
    getGame(gameId);
  }

  private void calcualatedGame(String json) throws Exception {
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

  static {
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"boughtIn\":false,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"rebought\":true,"
        + "    \"place\":1,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"firstName\":\"Doyle\","
        + "    \"lastName\":\"Brunson\","
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("{"
        + "  \"firstName\":\"Doyle\","
        + "  \"lastName\":\"Brunson\","
        + "  \"boughtIn\":true,"
        + "  \"annualTocParticipant\":false,"
        + "  \"quarterlyTocParticipant\":false,"
        + "  \"rebought\":true,"
        + "  \"place\":null,"
        + "  \"chop\":null"
        + "}");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("{"
        + "  \"boughtIn\":true,"
        + "  \"annualTocParticipant\":false,"
        + "  \"quarterlyTocParticipant\":false,"
        + "  \"rebought\":false,"
        + "  \"place\":null,"
        + "  \"chop\":null"
        + "}");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  }"
        + "]");
    GAME_PLAYERS.add("["
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"rebought\":false,"
        + "    \"place\":5,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":true,"
        + "    \"place\":4,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":true,"
        + "    \"place\":9,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"rebought\":true,"
        + "    \"place\":7,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":false,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":null,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":true,"
        + "    \"rebought\":true,"
        + "    \"place\":6,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":false,"
        + "    \"place\":1,"
        + "    \"chop\":55000"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":true,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":true,"
        + "    \"place\":2,"
        + "    \"chop\":48750"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":true,"
        + "    \"place\":8,"
        + "    \"chop\":null"
        + "  },"
        + "  {"
        + "    \"boughtIn\":true,"
        + "    \"annualTocParticipant\":false,"
        + "    \"quarterlyTocParticipant\":false,"
        + "    \"rebought\":true,"
        + "    \"place\":3,"
        + "    \"chop\":null"
        + "  }"
        + "]");

    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":0,"
        + "  \"rebuyAddOnCollected\":0,"
        + "  \"annualTocCollected\":0,"
        + "  \"quarterlyTocCollected\":0,"
        + "  \"totalCollected\":0,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":0,"
        + "  \"totalCombinedTocCalculated\":0,"
        + "  \"kittyCalculated\":0,"
        + "  \"prizePotCalculated\":0,"
        + "  \"numPlayers\":1,"
        + "  \"numPaidPlayers\":0,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"finalized\":false,"
        + "  \"payouts\":[]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":40,"
        + "  \"rebuyAddOnCollected\":40,"
        + "  \"annualTocCollected\":20,"
        + "  \"quarterlyTocCollected\":20,"
        + "  \"totalCollected\":120,"
        + "  \"annualTocFromRebuyAddOnCalculated\":20,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":20,"
        + "  \"totalCombinedTocCalculated\":60,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":50,"
        + "  \"numPlayers\":1,"
        + "  \"numPaidPlayers\":1,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":50,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":80,"
        + "  \"rebuyAddOnCollected\":0,"
        + "  \"annualTocCollected\":20,"
        + "  \"quarterlyTocCollected\":20,"
        + "  \"totalCollected\":120,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":0,"
        + "  \"totalCombinedTocCalculated\":40,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":70,"
        + "  \"numPlayers\":2,"
        + "  \"numPaidPlayers\":2,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":70,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":80,"
        + "  \"rebuyAddOnCollected\":40,"
        + "  \"annualTocCollected\":20,"
        + "  \"quarterlyTocCollected\":20,"
        + "  \"totalCollected\":160,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":40,"
        + "  \"totalCombinedTocCalculated\":40,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":110,"
        + "  \"numPlayers\":2,"
        + "  \"numPaidPlayers\":2,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":110,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":280,"
        + "  \"rebuyAddOnCollected\":0,"
        + "  \"annualTocCollected\":0,"
        + "  \"quarterlyTocCollected\":0,"
        + "  \"totalCollected\":280,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":0,"
        + "  \"totalCombinedTocCalculated\":0,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":270,"
        + "  \"numPlayers\":7,"
        + "  \"numPaidPlayers\":7,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":270,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":320,"
        + "  \"rebuyAddOnCollected\":0,"
        + "  \"annualTocCollected\":0,"
        + "  \"quarterlyTocCollected\":0,"
        + "  \"totalCollected\":320,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":0,"
        + "  \"totalCombinedTocCalculated\":0,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":310,"
        + "  \"numPlayers\":8,"
        + "  \"numPaidPlayers\":8,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":201,"
        + "      \"chopAmount\":null"
        + "    },"
        + "    {"
        + "      \"place\":2,"
        + "      \"amount\":109,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":320,"
        + "  \"rebuyAddOnCollected\":0,"
        + "  \"annualTocCollected\":0,"
        + "  \"quarterlyTocCollected\":0,"
        + "  \"totalCollected\":320,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":0,"
        + "  \"totalCombinedTocCalculated\":0,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":310,"
        + "  \"numPlayers\":8,"
        + "  \"numPaidPlayers\":8,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":201,"
        + "      \"chopAmount\":null"
        + "    },"
        + "    {"
        + "      \"place\":2,"
        + "      \"amount\":109,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":280,"
        + "  \"rebuyAddOnCollected\":0,"
        + "  \"annualTocCollected\":0,"
        + "  \"quarterlyTocCollected\":0,"
        + "  \"totalCollected\":280,"
        + "  \"annualTocFromRebuyAddOnCalculated\":0,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":0,"
        + "  \"totalCombinedTocCalculated\":0,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":270,"
        + "  \"numPlayers\":7,"
        + "  \"numPaidPlayers\":7,"
        + "  \"chopped\":false,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":270,"
        + "      \"chopAmount\":null"
        + "    }"
        + "  ]"
        + "}");
    GAME_CALCULATIONS.add("{"
        + "  \"buyInCollected\":360,"
        + "  \"rebuyAddOnCollected\":280,"
        + "  \"annualTocCollected\":120,"
        + "  \"quarterlyTocCollected\":60,"
        + "  \"totalCollected\":820,"
        + "  \"annualTocFromRebuyAddOnCalculated\":80,"
        + "  \"rebuyAddOnLessAnnualTocCalculated\":200,"
        + "  \"totalCombinedTocCalculated\":260,"
        + "  \"kittyCalculated\":10,"
        + "  \"prizePotCalculated\":550,"
        + "  \"numPlayers\":10,"
        + "  \"numPaidPlayers\":9,"
        + "  \"chopped\":true,"
        + "  \"canRebuy\":true,"
        + "  \"payouts\":["
        + "    {"
        + "      \"place\":1,"
        + "      \"amount\":357,"
        + "      \"chopAmount\":280"
        + "    },"
        + "    {"
        + "      \"place\":2,"
        + "      \"amount\":193,"
        + "      \"chopAmount\":270"
        + "    }"
        + "  ]"
        + "}");
  }

}
