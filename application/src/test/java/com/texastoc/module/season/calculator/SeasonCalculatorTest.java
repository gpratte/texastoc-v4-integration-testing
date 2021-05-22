package com.texastoc.module.season.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.model.SeasonPlayer;
import com.texastoc.module.season.repository.SeasonPayoutSettingsRepository;
import com.texastoc.module.season.repository.SeasonRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SeasonCalculatorTest implements TestConstants {

  private SeasonCalculator seasonCalculator;

  private SeasonRepository seasonRepository;
  private SeasonPayoutSettingsRepository seasonPayoutSettingsRepository;
  private GameModule gameModule;

  @Before
  public void before() {
    seasonRepository = mock(SeasonRepository.class);
    seasonPayoutSettingsRepository = mock(SeasonPayoutSettingsRepository.class);
    seasonCalculator = new SeasonCalculator(seasonRepository, seasonPayoutSettingsRepository);
    gameModule = mock(GameModule.class);
    ReflectionTestUtils.setField(seasonCalculator, "gameModule", gameModule);
  }

  @Test
  public void testNoGames() {
    // Arrange
    when(seasonRepository.findById(1))
        .thenReturn(Optional.of(Season.builder()
            .id(1)
            .start(LocalDate.of(2020, 5, 1))
            .build()));
    when(gameModule.getBySeasonId(1)).thenReturn(Collections.emptyList());
    when(seasonPayoutSettingsRepository.findByStartYear(2020))
        .thenReturn(Collections.singletonList(TestConstants.getSeasonPayoutSettings(2020)));

    // Act
    seasonCalculator.calculate(1);

    // Assert
    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
    Mockito.verify(seasonRepository, Mockito.times(1)).save(seasonArg.capture());
    Season season = seasonArg.getValue();

    assertEquals("season id should be 1", 1, season.getId());
    assertEquals("numGamesPlayed should be 0", 0, season.getNumGamesPlayed());
    assertEquals("buyInCollected should be 0", 0, season.getBuyInCollected());
    assertEquals("rebuyAddOnCollected should be 0", 0, season.getRebuyAddOnCollected());
    assertEquals("annualTocCollected should be 0", 0, season.getAnnualTocCollected());
    assertEquals("totalCollected should be 0", 0, season.getTotalCollected());

    assertEquals("annualTocFromRebuyAddOnCalculated should be 0", 0,
        season.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals("rebuyAddOnLessAnnualTocCalculated should be 0", 0,
        season.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals("totalCombinedAnnualTocCalculated should be 0", 0,
        season.getTotalCombinedAnnualTocCalculated());
    assertEquals("kittyCalculated should be 0", 0, season.getKittyCalculated());
    assertEquals("prizePotCalculated should be 0", 0, season.getPrizePotCalculated());

    assertEquals("numGamesPlayed should be 0", 0, season.getNumGamesPlayed());

    Assert.assertTrue("last calculated should be within the last few seconds",
        season.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    assertEquals(0, season.getPlayers().size());
    assertEquals(0, season.getPayouts().size());
    assertEquals(0, season.getEstimatedPayouts().size());
  }

  @Test
  public void test1Game() {
    // Arrange
    when(seasonRepository.findById(1))
        .thenReturn(Optional.of(Season.builder()
            .id(1)
            .start(LocalDate.of(2020, 5, 1))
            .numGames(52)
            .build()));
    when(seasonPayoutSettingsRepository.findByStartYear(2020))
        .thenReturn(Collections.singletonList(TestConstants.getSeasonPayoutSettings(2020)));

    // 10 players bought in
    int boughtIn = 10 * GAME_BUY_IN;
    // 6 annual toc
    int toc = 6 * TOC_PER_GAME;
    // 5 rebought
    int rebought = 5 * GAME_REBUY;
    // total collected
    int totalCollected = boughtIn + toc + rebought;
    // 3 of the rebuys are annual toc
    int tocFromRebuys = 3 * GAME_REBUY_TOC_DEBIT;
    // total toc
    int totalToc = toc + tocFromRebuys;
    // kitty
    int kitty = KITTY_PER_GAME;
    // prize pot
    int prizePot = totalCollected - totalToc - kitty;

    Game game = Game.builder()
        .buyInCollected(boughtIn)
        .annualTocCollected(toc)
        .rebuyAddOnCollected(rebought)
        .totalCollected(totalCollected)
        .annualTocFromRebuyAddOnCalculated(tocFromRebuys)
        .rebuyAddOnLessAnnualTocCalculated(rebought - tocFromRebuys)
        .totalCombinedTocCalculated(totalToc)
        .kittyCalculated(kitty)
        .prizePotCalculated(prizePot)
        .build();

    List<GamePlayer> gameSeasonPlayers = new ArrayList<>(10);
    for (int i = 0; i < 10; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .tocPoints(i < 6 ? i : 0)
          .annualTocParticipant(i < 6)
          .build();
      gameSeasonPlayers.add(gamePlayer);
    }
    game.setPlayers(gameSeasonPlayers);

    when(gameModule.getBySeasonId(1)).thenReturn(Collections.singletonList(game));

    // Act
    seasonCalculator.calculate(1);

    // Assert
    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
    Mockito.verify(seasonRepository, Mockito.times(1)).save(seasonArg.capture());
    Season season = seasonArg.getValue();

    assertEquals(1, season.getId());
    assertEquals(1, season.getNumGamesPlayed());
    assertEquals(boughtIn, season.getBuyInCollected());
    assertEquals(rebought, season.getRebuyAddOnCollected());
    assertEquals(toc, season.getAnnualTocCollected());
    assertEquals(totalCollected, season.getTotalCollected());
    assertEquals(tocFromRebuys, season.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals(rebought - tocFromRebuys, season.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals(totalToc, season.getTotalCombinedAnnualTocCalculated());
    assertEquals(kitty, season.getKittyCalculated());
    assertEquals(prizePot, season.getPrizePotCalculated());

    assertEquals(1, season.getNumGamesPlayed());

    Assert.assertTrue("last calculated should be within the last few seconds",
        season.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    assertEquals(6, season.getPlayers().size());
    // Season players are sorted by points
    SeasonPlayer seasonPlayer = season.getPlayers().get(0);
    assertEquals(1, seasonPlayer.getPlace().intValue());
    assertEquals(5, seasonPlayer.getPoints());
    seasonPlayer = season.getPlayers().get(1);
    assertEquals(2, seasonPlayer.getPlace().intValue());
    assertEquals(4, seasonPlayer.getPoints());
    seasonPlayer = season.getPlayers().get(2);
    assertEquals(3, seasonPlayer.getPlace().intValue());
    assertEquals(3, seasonPlayer.getPoints());
    seasonPlayer = season.getPlayers().get(3);
    assertEquals(4, seasonPlayer.getPlace().intValue());
    assertEquals(2, seasonPlayer.getPoints());
    seasonPlayer = season.getPlayers().get(4);
    assertEquals(5, seasonPlayer.getPlace().intValue());
    assertEquals(1, seasonPlayer.getPoints());
    seasonPlayer = season.getPlayers().get(5);
    assertNull(seasonPlayer.getPlace());
    assertEquals(0, seasonPlayer.getPoints());

    // TODO beef up testing payouts
    assertEquals(0, season.getPayouts().size());

    // TODO beef up testing estimated payouts
    assertEquals(5, season.getEstimatedPayouts().size());
  }

  @Test
  public void test2Games() {
    // Arrange
    when(seasonRepository.findById(1))
        .thenReturn(Optional.of(Season.builder()
            .id(1)
            .start(LocalDate.of(2020, 5, 1))
            .numGames(52)
            .build()));
    when(seasonPayoutSettingsRepository.findByStartYear(2020))
        .thenReturn(Collections.singletonList(TestConstants.getSeasonPayoutSettings(2020)));

    // Game 1
    // 10 players bought in
    int boughtIn1 = 10 * GAME_BUY_IN;
    // 6 annual toc
    int toc1 = 6 * TOC_PER_GAME;
    // 5 rebought
    int rebought1 = 5 * GAME_REBUY;
    // total collected
    int totalCollected1 = boughtIn1 + toc1 + rebought1;
    // 3 of the rebuys are annual toc
    int tocFromRebuys1 = 3 * GAME_REBUY_TOC_DEBIT;
    // total toc
    int totalToc1 = toc1 + tocFromRebuys1;
    // kitty
    int kitty1 = KITTY_PER_GAME;
    // prize pot
    int prizePot1 = totalCollected1 - totalToc1 - kitty1;

    Game game1 = Game.builder()
        .buyInCollected(boughtIn1)
        .annualTocCollected(toc1)
        .rebuyAddOnCollected(rebought1)
        .totalCollected(totalCollected1)
        .annualTocFromRebuyAddOnCalculated(tocFromRebuys1)
        .rebuyAddOnLessAnnualTocCalculated(rebought1 - tocFromRebuys1)
        .totalCombinedTocCalculated(totalToc1)
        .kittyCalculated(kitty1)
        .prizePotCalculated(prizePot1)
        .build();

    List<GamePlayer> gameSeasonPlayers = new ArrayList<>(10);
    for (int i = 0; i < 10; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .tocPoints(i)
          .annualTocParticipant(i < 6)
          .build();
      gameSeasonPlayers.add(gamePlayer);
    }
    game1.setPlayers(gameSeasonPlayers);

    // Game 2
    // 16 players bought in
    int boughtIn2 = 16 * GAME_BUY_IN;
    // 10 annual toc
    int toc2 = 10 * TOC_PER_GAME;
    // 5 rebought
    int rebought2 = 5 * GAME_REBUY;
    // total collected
    int totalCollected2 = boughtIn2 + toc2 + rebought2;
    // 3 of the rebuys are annual toc
    int tocFromRebuys2 = 3 * GAME_REBUY_TOC_DEBIT;
    // total toc
    int totalToc2 = toc2 + tocFromRebuys2;
    // kitty
    int kitty2 = KITTY_PER_GAME;
    // prize pot
    int prizePot2 = totalCollected2 - totalToc2 - kitty2;

    Game game2 = Game.builder()
        .buyInCollected(boughtIn2)
        .annualTocCollected(toc2)
        .rebuyAddOnCollected(rebought2)
        .totalCollected(totalCollected2)
        .annualTocFromRebuyAddOnCalculated(tocFromRebuys2)
        .rebuyAddOnLessAnnualTocCalculated(rebought2 - tocFromRebuys2)
        .totalCombinedTocCalculated(totalToc2)
        .kittyCalculated(kitty2)
        .prizePotCalculated(prizePot2)
        .build();

    gameSeasonPlayers = new ArrayList<>(10);
    for (int i = 0; i < 16; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .tocPoints(i)
          .annualTocParticipant(i < 10)
          .build();
      gameSeasonPlayers.add(gamePlayer);
    }

    game2.setPlayers(gameSeasonPlayers);

    when(gameModule.getBySeasonId(1)).thenReturn(Arrays.asList(game1, game2));

    // Act
    seasonCalculator.calculate(1);

    // Assert
    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
    Mockito.verify(seasonRepository, Mockito.times(1)).save(seasonArg.capture());
    Season season = seasonArg.getValue();

    assertEquals(1, season.getId());
    assertEquals(2, season.getNumGamesPlayed());
    assertEquals(boughtIn1 + boughtIn2, season.getBuyInCollected());
    assertEquals(rebought1 + rebought2, season.getRebuyAddOnCollected());
    assertEquals(toc1 + toc2, season.getAnnualTocCollected());
    assertEquals(totalCollected1 + totalCollected2, season.getTotalCollected());
    assertEquals(tocFromRebuys1 + tocFromRebuys2, season.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals((rebought1 - tocFromRebuys1) + (rebought2 - tocFromRebuys2),
        season.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals(totalToc1 + totalToc2, season.getTotalCombinedAnnualTocCalculated());
    assertEquals(kitty1 + kitty2, season.getKittyCalculated());
    assertEquals(prizePot1 + prizePot2, season.getPrizePotCalculated());

    assertEquals(2, season.getNumGamesPlayed());

    Assert.assertTrue("last calculated should be within the last few seconds",
        season.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    assertEquals(10, season.getPlayers().size());
    // Season players are sorted by points
    SeasonPlayer seasonPlayer = season.getPlayers().get(0);
    assertEquals(1, seasonPlayer.getPlace().intValue());
    assertEquals(10, seasonPlayer.getPoints());
    assertEquals(2, seasonPlayer.getEntries());

    seasonPlayer = season.getPlayers().get(1);
    assertEquals(2, seasonPlayer.getPlace().intValue());
    assertEquals(9, seasonPlayer.getPoints());
    assertEquals(1, seasonPlayer.getEntries());

    seasonPlayer = season.getPlayers().get(2);
    assertEquals(3, seasonPlayer.getPlace().intValue());
    assertEquals(8, seasonPlayer.getPoints());

    seasonPlayer = season.getPlayers().get(3);
    assertEquals(3, seasonPlayer.getPlace().intValue());
    assertEquals(8, seasonPlayer.getPoints());

    seasonPlayer = season.getPlayers().get(4);
    assertEquals(5, seasonPlayer.getPlace().intValue());
    assertEquals(7, seasonPlayer.getPoints());
    assertEquals(1, seasonPlayer.getEntries());

    seasonPlayer = season.getPlayers().get(5);
    assertEquals(6, seasonPlayer.getPlace().intValue());
    assertEquals(6, seasonPlayer.getPoints());

    seasonPlayer = season.getPlayers().get(6);
    assertEquals(6, seasonPlayer.getPlace().intValue());
    assertEquals(6, seasonPlayer.getPoints());

    seasonPlayer = season.getPlayers().get(7);
    assertEquals(8, seasonPlayer.getPlace().intValue());
    assertEquals(4, seasonPlayer.getPoints());
    assertEquals(2, seasonPlayer.getEntries());

    seasonPlayer = season.getPlayers().get(8);
    assertEquals(9, seasonPlayer.getPlace().intValue());
    assertEquals(2, seasonPlayer.getPoints());
    assertEquals(2, seasonPlayer.getEntries());

    seasonPlayer = season.getPlayers().get(9);
    assertNull(seasonPlayer.getPlace());
    assertEquals(0, seasonPlayer.getPoints());

    // TODO beef up testing payouts
    assertEquals(0, season.getPayouts().size());

    // TODO beef up testing estimated payouts
    assertEquals(5, season.getEstimatedPayouts().size());
  }

}
