package com.texastoc.module.game.calculator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.texastoc.TestConstants;
import com.texastoc.TestUtils;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class GameCalculatorTest implements TestConstants {

  private GameCalculator gameCalculator;

  static final Random RANDOM = new Random(System.currentTimeMillis());

  private GameRepository gameRepository;

  @Before
  public void before() {
    gameRepository = mock(GameRepository.class);
    gameCalculator = new GameCalculator(gameRepository);
  }

  @Test
  public void testNullGamePlayers() {
    Game gameToCalculate = Game.builder()
        .id(1)
        .build();
    TestUtils.populateGameCosts(gameToCalculate);
    gameCalculator.calculate(gameToCalculate);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();
    assertGameNoPlayers(gameCalculated);
  }

  @Test
  public void testEmptyGamePlayers() {
    Game gameToCalculate = Game.builder()
        .id(1)
        .players(Collections.emptyList())
        .build();
    TestUtils.populateGameCosts(gameToCalculate);
    gameCalculator.calculate(gameToCalculate);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();
    assertGameNoPlayers(gameCalculated);
  }

  @Test
  public void testGamePlayersNoBuyIns() {
    List<GamePlayer> gamePlayers = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .build();
      gamePlayers.add(gamePlayer);
    }

    Game gameToCalculate = Game.builder()
        .id(1)
        .players(gamePlayers)
        .build();
    TestUtils.populateGameCosts(gameToCalculate);
    gameCalculator.calculate(gameToCalculate);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();
    assertGameNoPlayers(gameCalculated);
  }

  /**
   * One of each means
   * <table>
   *     <tr>
   *         <th>Player Id</th>
   *         <th>Bought-in</th>
   *         <th>Annual TOC participant</th>
   *         <th>Quarterly TOC participant</th>
   *         <th>Rebought</th>
   *     </tr>
   *     <tr>
   *         <td>1</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *     </tr>
   *     <tr>
   *         <td>2</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *     </tr>
   *     <tr>
   *         <td>3</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *     </tr>
   *     <tr>
   *         <td>4</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *     </tr>
   *
   *     <tr>
   *         <td>5</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *     </tr>
   *     <tr>
   *         <td>6</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *     </tr>
   *     <tr>
   *         <td>7</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *     </tr>
   *     <tr>
   *         <td>8</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *     </tr>
   * </table>
   * The results for game should be (given the TestConstants)
   * <ul>
   *     <li>Number of players = 8</li>
   *     <li>Kitty collected = 9</li>
   *     <li>Buy-in collected = 48</li>
   *     <li>Rebuy Add On collected = 20</li>
   *     <li>Annual TOC collected = 32</li>
   *     <li>Quarterly TOC collected = 28</li>
   *     <li>Rebuy Add On TOC collected = 8</li>
   *     <li>Total collected = 48 (buy-in) + 20 (rebuy) + 32 (toc) + 28 (qtoc) = 128</li>
   *     <li>Total TOC collected = 32 (toc) + 28 (qtoc) + 8 (rebuy toc) = 68</li>
   *     <li>prizePot = total collected minus total toc minus kitty =  51</li>
   * </ul>
   */
  @Test
  public void testGamePlayerOneOfEach() {
    List<GamePlayer> gamePlayers = new ArrayList<>();
    GamePlayer gamePlayer = GamePlayer.builder()
        .id(1)
        .playerId(1)
        .gameId(1)
        .boughtIn(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(2)
        .playerId(2)
        .gameId(1)
        .boughtIn(true)
        .annualTocParticipant(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(3)
        .playerId(3)
        .gameId(1)
        .boughtIn(true)
        .quarterlyTocParticipant(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(4)
        .playerId(4)
        .gameId(1)
        .boughtIn(true)
        .annualTocParticipant(true)
        .quarterlyTocParticipant(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(5)
        .playerId(5)
        .gameId(1)
        .boughtIn(true)
        .rebought(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(6)
        .playerId(6)
        .gameId(1)
        .boughtIn(true)
        .annualTocParticipant(true)
        .rebought(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(7)
        .playerId(7)
        .gameId(1)
        .boughtIn(true)
        .quarterlyTocParticipant(true)
        .rebought(true)
        .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
        .id(8)
        .playerId(8)
        .gameId(1)
        .boughtIn(true)
        .annualTocParticipant(true)
        .quarterlyTocParticipant(true)
        .rebought(true)
        .build();
    gamePlayers.add(gamePlayer);

    Game gameToCalculate = Game.builder()
        .id(1)
        .players(gamePlayers)
        .build();
    TestUtils.populateGameCosts(gameToCalculate);
    gameCalculator.calculate(gameToCalculate);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();

    assertEquals("number of game players should be 8", 8, gameCalculated.getNumPlayers());

    assertEquals("buy-in collected should be 48", 48, gameCalculated.getBuyInCollected());
    assertEquals("rebuy add on collected should be 20", 20,
        gameCalculated.getRebuyAddOnCollected());
    assertEquals("annual toc collected should be 32", 32, gameCalculated.getAnnualTocCollected());
    assertEquals("quarterly toc collected should be 28", 28,
        gameCalculated.getQuarterlyTocCollected());
    assertEquals("total collected should be 128", 128, gameCalculated.getTotalCollected());

    assertEquals("kitty calculated should be 9", 9, gameCalculated.getKittyCalculated());
    assertEquals("annual Toc from rebuy add on calculated should be 8", 8,
        gameCalculated.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals("rebuy add on less annual Toc calculated should be 12", 12,
        gameCalculated.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals("total combined toc calculated should be 68", 68,
        gameCalculated.getTotalCombinedTocCalculated());
    assertEquals("prize pot calculated should be 51", 51, gameCalculated.getPrizePotCalculated());
  }

  private void assertGameNoPlayers(Game gameCalculated) {
    assertEquals("buy-in collected should be 0", 0, gameCalculated.getBuyInCollected());
    assertEquals("rebuy add on collected should be 0", 0, gameCalculated.getRebuyAddOnCollected());
    assertEquals("annual toc collected should be 0", 0, gameCalculated.getAnnualTocCollected());
    assertEquals("quarterly toc collected should be 0", 0,
        gameCalculated.getQuarterlyTocCollected());
    assertEquals("total collected should be 0", 0, gameCalculated.getTotalCollected());

    assertEquals("rebuy add on toc calculated should be 0", 0,
        gameCalculated.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals("rebuy add on less toc calculated should be 0", 0,
        gameCalculated.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals("total toc calculated should be 0", 0,
        gameCalculated.getTotalCombinedTocCalculated());
    assertEquals("kitty calculated should be 0", 0, gameCalculated.getKittyCalculated());
    assertEquals("prize pot should be 0", 0, gameCalculated.getPrizePotCalculated());
  }
}
