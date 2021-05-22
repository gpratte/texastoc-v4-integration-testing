package com.texastoc.module.game.calculator;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.texastoc.TestConstants;
import com.texastoc.common.PointsGenerator;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class PointsCalculatorTest implements TestConstants {

  private PointsCalculator pointsCalculator;
  private GameRepository gameRepository;
  private PointsGenerator pointsGenerator;

  @Before
  public void before() {
    gameRepository = mock(GameRepository.class);
    pointsGenerator = new PointsGenerator(CHOP_TENTH_PLACE_INCR,
        CHOP_TENTH_PLACE_POINTS,
        CHOP_MULTIPLIER);
    pointsCalculator = new PointsCalculator(pointsGenerator, gameRepository);
  }

  @Test
  public void testNoPlayersNoPoints() {
    Game game = Game.builder()
        .id(1)
        .numPlayers(0)
        .players(Collections.emptyList())
        .build();

    pointsCalculator.calculate(game);
    verify(gameRepository, times(1)).save(any());
  }

  @Test
  public void test1PlayersNoPoints() {
    List<GamePlayer> gamePlayers = new ArrayList<>(1);
    gamePlayers.add(GamePlayer.builder()
        .build());

    Game game = Game.builder()
        .id(1)
        .numPlayers(1)
        .players(gamePlayers)
        .build();

    pointsCalculator.calculate(game);
    verify(gameRepository, times(1)).save(any());
  }

  /*
   * Of the seven players
   * 1st place gets toc and qtoc points and
   * 3rd place gets toc points and
   * 5th gets qtoc points
   */
  @Test
  public void test7Players() {
    int numPlayers = 7;
    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
          .place(i + 1)
          .build());
    }

    // first place both toc and qtoc
    gamePlayers.get(0).setAnnualTocParticipant(true);
    gamePlayers.get(0).setQuarterlyTocParticipant(true);
    int expectedFirstPlacePoints = pointsGenerator.generatePlacePoints(7).get(1);

    // third place toc
    gamePlayers.get(2).setAnnualTocParticipant(true);
    int expectedThirdPlacePoints = pointsGenerator.generatePlacePoints(7).get(3);

    // fifth place qtoc
    gamePlayers.get(4).setQuarterlyTocParticipant(true);
    int expectedFifthPlacePoints = pointsGenerator.generatePlacePoints(7).get(5);

    Game game = Game.builder()
        .id(1)
        .numPlayers(numPlayers)
        .players(gamePlayers)
        .build();

    pointsCalculator.calculate(game);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();

    List<GamePlayer> actualGamePlayers = gameCalculated.getPlayers();

    // 1st place
    assertEquals(expectedFirstPlacePoints, actualGamePlayers.get(0).getTocPoints().intValue());
    assertEquals(expectedFirstPlacePoints, actualGamePlayers.get(0).getQTocPoints().intValue());

    // 2nd place
    Assert.assertNull(actualGamePlayers.get(1).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(1).getQTocPoints());

    // 3rd place
    assertEquals(expectedThirdPlacePoints, actualGamePlayers.get(2).getTocPoints().intValue());
    Assert.assertNull(actualGamePlayers.get(2).getQTocPoints());

    // 4th place
    Assert.assertNull(actualGamePlayers.get(3).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(3).getQTocPoints());

    // 5th place
    Assert.assertNull(actualGamePlayers.get(4).getTocPoints());
    assertEquals(expectedFifthPlacePoints, actualGamePlayers.get(4).getQTocPoints().intValue());

    // 6th place
    Assert.assertNull(actualGamePlayers.get(5).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(5).getQTocPoints());

    // 7th place
    Assert.assertNull(actualGamePlayers.get(6).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(6).getQTocPoints());
  }

  /*
   * 1st, 2nd and 3rd chop
   * Odd numbers toc
   * 2nd place toc
   * Multiples of 3 are qtoc
   */
  @Test
  public void test22PlayersWith3Chopped() {
    int numPlayers = 22;
    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      int place = i + 1;
      boolean isOdd = isOdd(place);
      boolean isMultipleOf3 = isMultipleOf3(place);
      gamePlayers.add(GamePlayer.builder()
          .place(place <= 11 ? place : null)
          .annualTocParticipant(isOdd || place == 2)
          .quarterlyTocParticipant(isMultipleOf3)
          .build());
    }

    int firstPlaceChips = 125_000;
    gamePlayers.get(0).setChop(firstPlaceChips);
    int secondPlaceChips = 98_750;
    gamePlayers.get(1).setChop(secondPlaceChips);
    int thirdPlaceChips = 33_250;
    gamePlayers.get(2).setChop(thirdPlaceChips);

    Game game = Game.builder()
        .id(1)
        .numPlayers(numPlayers)
        .players(gamePlayers)
        .build();

    pointsCalculator.calculate(game);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();

    List<GamePlayer> actualGamePlayers = gameCalculated.getPlayers();

    for (int i = 0; i < actualGamePlayers.size(); i++) {
      GamePlayer actual = actualGamePlayers.get(i);
      Integer place = actual.getPlace();
      boolean isTopTen = place != null && place < 11;
      boolean isOdd = isOdd(place);
      boolean isMultipleOf3 = isMultipleOf3(place);
      if (isTopTen) {
        if (isOdd || place == 2) {
          assertEquals(pointsGenerator.generatePlacePoints(numPlayers).get(i + 1),
              actual.getTocPoints());
        } else {
          Assert.assertNull(actual.getTocPoints());
        }
        if (isMultipleOf3) {
          assertEquals(pointsGenerator.generatePlacePoints(numPlayers).get(i + 1),
              actual.getQTocPoints());
        } else {
          Assert.assertNull(actual.getQTocPoints());
        }

        // From https://www.primedope.com/icm-deal-calculator/
        // with stacks 125000, 98750, 33250
        // and prizes 130, 100, 78
        // points should be 112, 107, 89
        if (actual.getPlace() == 1) {
          assertEquals(112, actual.getTocChopPoints().intValue());
        }
        if (actual.getPlace() == 2) {
          assertEquals(107, actual.getTocChopPoints().intValue());
        }
        if (actual.getPlace() == 3) {
          assertEquals(89, actual.getTocChopPoints().intValue());
          assertEquals(89, actual.getQTocChopPoints().intValue());
        }
      } else {
        Assert.assertNull(actual.getTocPoints());
        Assert.assertNull(actual.getQTocPoints());
      }
    }
  }

  private boolean isOdd(Integer num) {
    return num != null && !(num % 2 == 0);
  }

  private boolean isMultipleOf3(Integer num) {
    return num != null && num % 3 == 0;
  }
}
