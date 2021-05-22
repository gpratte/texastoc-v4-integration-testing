package com.texastoc.module.game.calculator;

import static com.texastoc.TestConstants.getSettings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.Payout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class PayoutCalculatorTest implements TestConstants {

  private PayoutCalculator payoutCalculator;
  private static final Random RANDOM = new Random(System.currentTimeMillis());

  private GameRepository gameRepository;

  @Before
  public void before() {
    gameRepository = mock(GameRepository.class);
    payoutCalculator = new PayoutCalculator(gameRepository);
    SettingsModule settingsModule = mock(SettingsModule.class);
    when(settingsModule.get()).thenReturn(getSettings());
    ReflectionTestUtils.setField(payoutCalculator, "settingsModule", settingsModule);
  }

  @Test
  public void testNoPlayersNoPayouts() {
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(0)
        .prizePotCalculated(0)
        .build();

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 0", 0, gamePayouts.size());
  }

  @Test
  public void test1PlayersNoPayouts() {
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(1)
        .prizePotCalculated(0)
        .build();

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 0", 0, gamePayouts.size());
  }

  @Test
  public void test1Players1Payout() {
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(1)
        .prizePotCalculated(GAME_BUY_IN)
        .build();

    when(gameRepository.findById(1)).thenReturn(Optional.of(new Game()));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    verify(gameRepository, Mockito.times(1)).findById(1);
    verify(gameRepository, Mockito.times(1)).save(any());

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 1", 1, gamePayouts.size());

    GamePayout gamePayout = gamePayouts.get(0);
    assertEquals("payout game id should be 1", 1, gamePayout.getGameId());
    assertEquals("payout should be place 1", 1, gamePayout.getPlace());
    assertEquals("payout amount should be " + GAME_BUY_IN, GAME_BUY_IN, gamePayout.getAmount());
    assertNull("payout chop amount should be null", gamePayout.getChopAmount());
  }

  @Test
  public void test7Players1Payout() {
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(7)
        .prizePotCalculated(GAME_BUY_IN * 7)
        .build();

    when(gameRepository.findById(1)).thenReturn(Optional.of(new Game()));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    verify(gameRepository, Mockito.times(1)).findById(1);
    verify(gameRepository, Mockito.times(1)).save(any());

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 1", 1, gamePayouts.size());

    GamePayout gamePayout = gamePayouts.get(0);
    assertEquals("payout should be place 1", 1, gamePayout.getPlace());
    assertEquals("payout amount should be " + (GAME_BUY_IN * 7), GAME_BUY_IN * 7,
        gamePayout.getAmount());
    assertNull("payout chop amount should be null", gamePayout.getChopAmount());
  }

  @Test
  public void test10Players2PayoutsWith2Chop() {
    int numPlayers = 10;
    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i + 1)
          .place(i + 1)
          .boughtIn(true)
          .build();
      gamePlayers.add(gamePlayer);
    }
    gamePlayers.get(0).setChop(100_000);
    gamePlayers.get(1).setChop(25_000);

    int prizePot = GAME_BUY_IN * numPlayers;
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(numPlayers)
        .players(gamePlayers)
        .prizePotCalculated(prizePot)
        .build();

    when(gameRepository.findById(1)).thenReturn(Optional.of(gameToCalculate));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    verify(gameRepository, Mockito.times(1)).findById(1);
    verify(gameRepository, Mockito.times(1)).save(any());

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

    List<Integer> amountsWithoutChop = new ArrayList<>(2);
    int firstPlaceWithoutChop = (int) Math
        .round(TestConstants.getPayouts(2).get(0).getPercent() * prizePot);
    int secondPlaceWithoutChop = (int) Math
        .round(TestConstants.getPayouts(2).get(1).getPercent() * prizePot);
    amountsWithoutChop.add(firstPlaceWithoutChop);
    amountsWithoutChop.add(secondPlaceWithoutChop);

    double leftoverWithoutChop = prizePot - firstPlaceWithoutChop - secondPlaceWithoutChop;
    leftoverWithoutChop = Math.abs(leftoverWithoutChop);

    // From https://www.primedope.com/icm-deal-calculator/
    List<Integer> amountsWithChop = new ArrayList<>(2);
    int firstPlaceWithChop = 35;
    int secondPlaceWithChop = 25;
    amountsWithChop.add(firstPlaceWithChop);
    amountsWithChop.add(secondPlaceWithChop);

    int totalPaidOutWithoutChop = 0;
    int totalPaidOutWithChop = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amountWithoutChop = amountsWithoutChop.get(i);
      int amountWithChop = amountsWithChop.get(i);
      int place = i + 1;

      assertEquals("place should be " + place, place, gamePayout.getPlace());
      assertEquals(amountWithoutChop, gamePayout.getAmount(), leftoverWithoutChop);
      assertEquals(amountWithChop, gamePayout.getChopAmount().intValue());
      totalPaidOutWithoutChop += gamePayout.getAmount();
      totalPaidOutWithChop += gamePayout.getChopAmount();
    }

    assertEquals("sum of payouts without chop for " + numPlayers + " players should be " + prizePot,
        prizePot, totalPaidOutWithoutChop);
    assertEquals("sum of payouts with chop for " + numPlayers + " players should be " + prizePot,
        prizePot, totalPaidOutWithChop);
  }

  @Test
  public void test10Players2PayoutsWith3Chop() {
    int numPlayers = 10;
    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i + 1)
          .place(i + 1)
          .boughtIn(true)
          .build();
      gamePlayers.add(gamePlayer);
    }
    gamePlayers.get(0).setChop(100_000);
    gamePlayers.get(1).setChop(50_000);
    gamePlayers.get(2).setChop(25_000);

    int prizePot = GAME_BUY_IN * numPlayers;
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(numPlayers)
        .players(gamePlayers)
        .prizePotCalculated(prizePot)
        .build();

    when(gameRepository.findById(1)).thenReturn(Optional.of(gameToCalculate));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    verify(gameRepository, Mockito.times(1)).findById(1);
    verify(gameRepository, Mockito.times(1)).save(any());

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 3", 3, gamePayouts.size());

    // From https://www.primedope.com/icm-deal-calculator/
    List<Integer> amountsWithChop = new ArrayList<>(3);
    int firstPlaceWithChop = 29;
    int secondPlaceWithChop = 20;
    int thirdPlaceWithChop = 11;
    amountsWithChop.add(firstPlaceWithChop);
    amountsWithChop.add(secondPlaceWithChop);
    amountsWithChop.add(thirdPlaceWithChop);

    int totalPaidOutWithoutChop = 0;
    int totalPaidOutWithChop = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amountWithChop = amountsWithChop.get(i);
      int place = i + 1;

      assertEquals("place should be " + place, place, gamePayout.getPlace());
      assertEquals(amountWithChop, gamePayout.getChopAmount().intValue());
      totalPaidOutWithoutChop += gamePayout.getAmount();
      totalPaidOutWithChop += gamePayout.getChopAmount();
    }

    assertEquals("sum of payouts without chop for " + numPlayers + " players should be " + prizePot,
        prizePot, totalPaidOutWithoutChop);
    assertEquals("sum of payouts with chop for " + numPlayers + " players should be " + prizePot,
        prizePot, totalPaidOutWithChop);
  }

  @Test
  public void test50Players10PayoutsWith3Chop() {
    int numPlayers = 50;
    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i + 1)
          .place(i + 1)
          .boughtIn(true)
          .build();
      gamePlayers.add(gamePlayer);
    }
    gamePlayers.get(0).setChop(550_000);
    gamePlayers.get(1).setChop(300_000);
    gamePlayers.get(2).setChop(25_000);

    int prizePot = GAME_BUY_IN * numPlayers;
    Game gameToCalculate = Game.builder()
        .id(1)
        .numPlayers(numPlayers)
        .players(gamePlayers)
        .prizePotCalculated(prizePot)
        .build();

    when(gameRepository.findById(1)).thenReturn(Optional.of(gameToCalculate));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(gameToCalculate);

    verify(gameRepository, Mockito.times(1)).findById(1);
    verify(gameRepository, Mockito.times(1)).save(any());

    assertNotNull("list of game payouts should not be null", gamePayouts);
    assertEquals("list of game payouts should be size 10", 10, gamePayouts.size());

    List<Integer> amountsWithoutChop = new ArrayList<>(10);
    List<Payout> payoutPercentages = TestConstants.getPayouts(10);
    int sumOfAmountsWithoutChop = 0;
    for (int i = 0; i < 10; i++) {
      double percentage = payoutPercentages.get(i).getPercent() * prizePot;
      int placeWithoutChop = (int) Math.round(percentage);
      amountsWithoutChop.add(placeWithoutChop);
      sumOfAmountsWithoutChop += placeWithoutChop;
    }

    double leftoverWithoutChop = prizePot - sumOfAmountsWithoutChop;
    leftoverWithoutChop = Math.abs(leftoverWithoutChop);

    // From https://www.primedope.com/icm-deal-calculator/
    // assuming 1st=91, 2nd=58, 3rd=41
    List<Integer> amountsWithChop = new ArrayList<>(3);
    amountsWithChop.add(78);
    amountsWithChop.add(68);
    amountsWithChop.add(44);

    int totalPaidOutWithoutChop = 0;
    int totalPaidOutWithChop = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amountWithoutChop = amountsWithoutChop.get(i);
      int amountWithChop = 0;
      if (i < amountsWithChop.size()) {
        amountWithChop = amountsWithChop.get(i);
      } else {
        amountWithChop = amountsWithoutChop.get(i);
      }
      int place = i + 1;

      assertEquals("place should be " + place, place, gamePayout.getPlace());
      assertEquals(amountWithoutChop, gamePayout.getAmount(), leftoverWithoutChop);
      if (i < amountsWithChop.size()) {
        assertEquals(amountWithChop, gamePayout.getChopAmount().intValue());
      }
      totalPaidOutWithoutChop += gamePayout.getAmount();
      if (i < amountsWithChop.size()) {
        totalPaidOutWithChop += gamePayout.getChopAmount();
      } else {
        totalPaidOutWithChop += gamePayout.getAmount();
      }
    }

    assertEquals("sum of payouts without chop for " + numPlayers + " players should be " + prizePot,
        prizePot, totalPaidOutWithoutChop);
    assertEquals("sum of payouts with chop for " + numPlayers + " players should be " + prizePot,
        prizePot, totalPaidOutWithChop);
  }
}
