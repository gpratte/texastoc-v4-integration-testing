package com.texastoc.module.game.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.texastoc.TestConstants;
import com.texastoc.module.game.event.GameEventProducer;
import com.texastoc.module.game.exception.GameInProgressException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.model.Quarter;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.model.Season;
import java.time.LocalDate;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class GameServiceTest implements TestConstants {

  private GameService gameService;

  private GameRepository gameRepository;
  private GameHelper gameHelper;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    gameHelper = mock(GameHelper.class);
    GameEventProducer gameEventProducer = mock(GameEventProducer.class);
    gameService = new GameService(gameRepository, gameHelper, gameEventProducer);
    playerModule = mock(PlayerModule.class);
    ReflectionTestUtils.setField(gameService, "playerModule", playerModule);
    seasonModule = mock(SeasonModule.class);
    ReflectionTestUtils.setField(gameService, "seasonModule", seasonModule);
    quarterlySeasonModule = mock(QuarterlySeasonModule.class);
    ReflectionTestUtils.setField(gameService, "quarterlySeasonModule", quarterlySeasonModule);
  }

  @Test
  public void testCreateGame() {
    // Arrange
    when(seasonModule.getCurrent())
        .thenReturn(Season.builder()
            .id(16)
            .kittyPerGameCost(KITTY_PER_GAME)
            .tocPerGameCost(TOC_PER_GAME)
            .quarterlyTocPerGameCost(QUARTERLY_TOC_PER_GAME)
            .quarterlyNumPayouts(QUARTERLY_NUM_PAYOUTS)
            .buyInCost(GAME_BUY_IN)
            .rebuyAddOnCost(GAME_REBUY)
            .rebuyAddOnTocDebitCost(GAME_REBUY_TOC_DEBIT)
            .numGamesPlayed(20)
            .build());

    // Other games are finalized
    when(gameRepository.findBySeasonId(16))
        .thenReturn(ImmutableList.of(Game.builder()
            .finalized(true)
            .build()));

    // Quarterly season
    LocalDate gameDate = LocalDate.now();
    when(quarterlySeasonModule.getByDate(gameDate))
        .thenReturn(QuarterlySeason.builder()
            .id(17)
            .quarter(Quarter.FIRST)
            .seasonId(16)
            .numGamesPlayed(8)
            .build());

    // Host
    Mockito.when(playerModule.get(55))
        .thenReturn(Player.builder()
            .id(1)
            .firstName("Johnny")
            .lastName("Host The Most")
            .build());

    Game expected = Game.builder()
        .date(gameDate)
        .hostId(55)
        .transportRequired(true)
        .build();

    // Act
    gameService.create(expected);

    // Assert
    // Game repository called once
    verify(gameRepository, Mockito.times(1)).save(any(Game.class));

    // Game argument match
    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    verify(gameRepository).save(gameArg.capture());
    Game actual = gameArg.getValue();

    // Assert
    assertNotNull(actual);
    assertEquals("SeasonId should be 16", 16, actual.getSeasonId());
    assertEquals("QuarterlySeasonId should be 17", 17, actual.getQSeasonId());
    assertEquals("Host id should be 55", expected.getHostId(), actual.getHostId());
    assertEquals("date should be now", expected.getDate(), actual.getDate());

    assertTrue("Host name should be Johnny Host The Most",
        "Johnny Host The Most".equals(actual.getHostName()));
    assertEquals("Quarter should be first", Quarter.FIRST, actual.getQuarter());
    assertNull("last calculated should be null", actual.getLastCalculated());

    // Game setup variables
    assertEquals("transport required", expected.isTransportRequired(),
        actual.isTransportRequired());
    assertEquals("Kitty cost should be amount set for season", KITTY_PER_GAME,
        actual.getKittyCost());
    assertEquals("Annual TOC be amount set for season", TOC_PER_GAME,
        actual.getAnnualTocCost());
    assertEquals("Quarterly TOC be amount set for season", QUARTERLY_TOC_PER_GAME,
        actual.getQuarterlyTocCost());

    // Game runtime variables
    assertNull("not started", actual.getStarted());

    assertEquals("No players", 0, actual.getNumPlayers());
    assertEquals("No buy in collected", 0, actual.getBuyInCollected());
    assertEquals("No rebuy collected", 0, actual.getRebuyAddOnCollected());
    assertEquals("No annual toc collected", 0, actual.getAnnualTocCollected());
    assertEquals("No quarterly toc collected", 0, actual.getQuarterlyTocCollected());
    assertEquals("total collected", 0, actual.getTotalCollected());

    assertEquals("no annualTocFromRebuyAddOnCalculated", 0,
        actual.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals("no rebuyAddOnLessAnnualTocCalculated", 0,
        actual.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals("no totalCombinedTocCalculated", 0, actual.getTotalCombinedTocCalculated());
    assertEquals("No kitty calculated", 0, actual.getKittyCalculated());
    assertEquals("no prizePotCalculated", 0, actual.getPrizePotCalculated());

    Assert.assertFalse("not finalized", actual.isFinalized());

    assertEquals("Buy in cost should be amount set for season", GAME_BUY_IN,
        actual.getBuyInCost());
    assertEquals("Rebuy cost should be amount set for season", GAME_REBUY,
        actual.getRebuyAddOnCost());
    assertEquals("Rebuy Toc debit cost should be amount set for season", GAME_REBUY_TOC_DEBIT,
        actual.getRebuyAddOnTocDebitCost());
  }

  @Test
  public void testCannotCreateGame() {
    // Arrange
    // Current season
    when(seasonModule.getCurrent())
        .thenReturn(Season.builder()
            .id(16)
            .build());

    // One other games is not finalized
    when(gameRepository.findBySeasonId(16))
        .thenReturn(ImmutableList.of(Game.builder()
            .finalized(false)
            .build()));

    // Act & Assert
    assertThatThrownBy(() -> {
      gameService.create(new Game());
    }).isInstanceOf(GameInProgressException.class)
        .hasMessageContaining("Action cannot be completed because there is a game in progress");
  }

  @Test
  public void testUpdateGame() {
    // Arrange
    Mockito.when(gameHelper.get(1)).thenReturn(Game.builder()
        .id(1)
        .hostId(0)
        .date(LocalDate.now().minusDays(1))
        .transportRequired(false)
        .finalized(false)
        .build());

    LocalDate now = LocalDate.now();
    Game expected = Game.builder()
        .id(1)
        .hostId(2)
        .date(now)
        .transportRequired(true)
        .build();

    // Act
    gameService.update(expected);

    // Assert
    verify(gameRepository, Mockito.times(1)).save(any(Game.class));
  }

  @Test
  public void testGetBySeasonId() {
    // Arrange
    when(gameRepository.findBySeasonId(1))
        .thenReturn(ImmutableList.of(Game.builder().id(1).build(),
            Game.builder().id(2).build()));

    // Act
    List<Game> games = gameService.getBySeasonId(1);

    // Assert
    assertEquals("expect two games", 2, games.size());
  }

  @Test
  public void testGet() {
    // Arrange
    when(gameHelper.get(123)).thenReturn(Game.builder()
        .id(123)
        .build());

    // Act
    Game game = gameService.get(123);

    // Assert
    assertEquals(123, game.getId());
  }

  @Test
  public void testGetCurrent() {
    // Arrange
    when(gameHelper.getCurrent()).thenReturn(Game.builder().id(123).build());

    // Act
    Game game = gameService.getCurrent();

    // Assert
    assertEquals(123, game.getId());
  }

  @Test
  public void testClearCacheGame() {
    // Act
    gameService.clearCacheGame();
  }

  @Test
  public void testGetByNoSeasonId() {
    // Arrange
    when(seasonModule.getCurrentId()).thenReturn(1);

    when(gameRepository.findBySeasonId(1))
        .thenReturn(ImmutableList.of(Game.builder().id(1).build(),
            Game.builder().id(2).build(),
            Game.builder().id(3).build()));

    // Act
    List<Game> games = gameService.getBySeasonId(null);

    // Assert
    assertEquals("expect three games", 3, games.size());
  }

  @Test
  public void testGetByQuarterlySeasonId() {
    // Arrange
    when(gameRepository.findByQuarterlySeasonId(1))
        .thenReturn(ImmutableList.of(Game.builder().id(1).build(),
            Game.builder().id(2).build()));

    // Act
    List<Game> games = gameService.getByQuarterlySeasonId(1);

    // Assert
    assertEquals("expect two games", 2, games.size());
  }

  @Test
  public void testFinalize() {
    // Arrange
    Mockito.when(gameHelper.get(1))
        .thenReturn(Game.builder()
            .id(1)
            .qSeasonId(1)
            .seasonId(1)
            .build());

    // Act
    gameService.finalize(1);

    // Assert
    verify(gameHelper, Mockito.times(2)).get(1);
    verify(gameHelper, Mockito.times(1)).recalculate(1);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    verify(gameRepository).save(gameArg.capture());
    Game actual = gameArg.getValue();

    assertTrue("game should be finalized", actual.isFinalized());
    assertNull("seating should be null", actual.getSeating());
  }

  @Test
  public void testAlreadyFinalize() {
    // Arrange
    Mockito.when(gameHelper.get(1))
        .thenReturn(Game.builder()
            .id(1)
            .seasonId(16)
            .finalized(true)
            .build());

    // Act
    gameService.finalize(1);

    // Assert
    verify(gameRepository, times(0)).save(any());
  }

  @Test
  public void testUnfinalize() {
    // Arrange
    Game gameToUnfinalize = Game.builder()
        .id(1)
        .seasonId(16)
        .finalized(true)
        .build();
    Mockito.when(gameHelper.get(1)).thenReturn(gameToUnfinalize);

    when(seasonModule.get(16))
        .thenReturn(Season.builder()
            .id(16)
            .finalized(false)
            .build());

    // Other games are finalized
    when(gameRepository.findBySeasonId(16))
        .thenReturn(ImmutableList.of(Game.builder()
                .finalized(true)
                .build(),
            gameToUnfinalize));

    // Act
    gameService.unfinalize(1);

    // Assert
    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    verify(gameRepository).save(gameArg.capture());
    Game actual = gameArg.getValue();

    assertFalse("game should be unfinalized", actual.isFinalized());
  }

  @Test
  public void testAlreadyUnfinalize() {
    // Arrange
    Mockito.when(gameHelper.get(1))
        .thenReturn(Game.builder()
            .id(1)
            .seasonId(16)
            .finalized(false)
            .build());

    // Act
    gameService.unfinalize(1);

    // Assert
    verify(gameRepository, times(0)).save(any());
  }

  @Test
  public void testCannotUnfinalized() {
    // Arrange
    Mockito.when(gameHelper.get(1))
        .thenReturn(Game.builder()
            .id(1)
            .seasonId(16)
            .finalized(true)
            .build());

    // Cannot unfinalize because the season is finalized
    when(seasonModule.get(16))
        .thenReturn(Season.builder()
            .id(16)
            .finalized(true)
            .build());

    // Act & Assert
    assertThatThrownBy(() -> {
      gameService.unfinalize(1);
    }).isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Cannot open a game when season is finalized");
  }

  @Test
  public void testCannotUnfinalized2() {
    // Arrange
    Mockito.when(gameHelper.get(1))
        .thenReturn(Game.builder()
            .id(1)
            .seasonId(16)
            .finalized(true)
            .build());

    when(seasonModule.get(16))
        .thenReturn(Season.builder()
            .id(16)
            .finalized(false)
            .build());

    // One other game is unfinalized
    when(gameRepository.findBySeasonId(16))
        .thenReturn(ImmutableList.of(Game.builder()
            .finalized(false)
            .build()));

    // Act & Assert
    assertThatThrownBy(() -> {
      gameService.unfinalize(1);
    }).isInstanceOf(GameInProgressException.class)
        .hasMessageContaining("Action cannot be completed because there is a game in progress");
  }

}
