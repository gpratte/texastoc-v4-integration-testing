package com.texastoc.module.quarterly.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.model.QuarterlySeasonPlayer;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
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

public class QuarterlySeasonCalculatorTest implements TestConstants {

  private QuarterlySeasonCalculator calculator;

  private QuarterlySeasonRepository repository;
  private GameModule gameModule;

  @Before
  public void setup() {
    repository = mock(QuarterlySeasonRepository.class);
    calculator = new QuarterlySeasonCalculator(repository);
    gameModule = mock(GameModule.class);
    ReflectionTestUtils.setField(calculator, "gameModule", gameModule);
  }

  @Test
  public void testNoGames() {
    // Arrange
    QuarterlySeason currentSeason = QuarterlySeason.builder()
        .id(1)
        .build();
    when(repository.findById(1)).thenReturn(Optional.of(currentSeason));
    when(gameModule.getByQuarterlySeasonId(1)).thenReturn(
        Collections.emptyList());

    // Act
    calculator.calculate(1);

    // Assert
    verify(repository, times(1)).findById(1);
    verify(gameModule, times(1)).getByQuarterlySeasonId(1);

    ArgumentCaptor<QuarterlySeason> argument = ArgumentCaptor.forClass(QuarterlySeason.class);
    verify(repository).save(argument.capture());
    QuarterlySeason qSeason = argument.getValue();

    assertNotNull("quarterly season not null", qSeason);
    assertEquals("quarterly season id 1", 1, (int) qSeason.getId());

    assertEquals("quarter has no games played", 0, qSeason.getNumGamesPlayed());
    assertEquals("qTocCollected is 0", 0, qSeason.getQTocCollected());

    assertEquals(0, qSeason.getPlayers().size());
    assertEquals(0, qSeason.getPayouts().size());
  }

  @Test
  public void test1Game() {
    // Arrange
    when(repository.findById(1))
        .thenReturn(Optional.of(QuarterlySeason.builder()
            .id(1)
            .seasonId(2)
            .numGames(13)
            .build()));

    // 4 quarterly toc
    int qToc = 4 * QUARTERLY_TOC_PER_GAME;
    Game game = Game.builder()
        .quarterlyTocCollected(qToc)
        .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(10);
    for (int i = 0; i < 10; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .qTocPoints(i < 4 ? i : 0)
          .quarterlyTocParticipant(i < 4)
          .build();
      gamePlayers.add(gamePlayer);
    }
    game.setPlayers(gamePlayers);

    when(gameModule.getByQuarterlySeasonId(1)).thenReturn(Collections.singletonList(game));

    // Act
    calculator.calculate(1);

    // Assert
    verify(repository, times(1)).findById(1);
    verify(gameModule, times(1)).getByQuarterlySeasonId(1);

    ArgumentCaptor<QuarterlySeason> argument = ArgumentCaptor.forClass(QuarterlySeason.class);
    verify(repository).save(argument.capture());
    QuarterlySeason qSeason = argument.getValue();

    assertNotNull("quarterly season not null", qSeason);
    assertEquals("quarterly season id 1", 1, (int) qSeason.getId());

    assertEquals(1, qSeason.getNumGamesPlayed());
    assertEquals(qToc, qSeason.getQTocCollected());

    assertEquals(4, qSeason.getPlayers().size());
    // Quarterly Season players are sorted by points
    QuarterlySeasonPlayer qSeasonPlayer = qSeason.getPlayers().get(0);
    assertEquals(1, qSeasonPlayer.getPlace().intValue());
    assertEquals(3, qSeasonPlayer.getPoints());
    qSeasonPlayer = qSeason.getPlayers().get(1);
    assertEquals(2, qSeasonPlayer.getPlace().intValue());
    assertEquals(2, qSeasonPlayer.getPoints());
    qSeasonPlayer = qSeason.getPlayers().get(2);
    assertEquals(3, qSeasonPlayer.getPlace().intValue());
    assertEquals(1, qSeasonPlayer.getPoints());
    qSeasonPlayer = qSeason.getPlayers().get(3);
    assertNull(qSeasonPlayer.getPlace());
    assertEquals(0, qSeasonPlayer.getPoints());

    // TODO beef up testing payouts
    assertEquals(3, qSeason.getPayouts().size());
  }

  @Test
  public void test2Games() {
    // Arrange
    when(repository.findById(1))
        .thenReturn(Optional.of(QuarterlySeason.builder()
            .id(1)
            .seasonId(2)
            .numGames(13)
            .build()));

    // Game 1
    // 4 annual toc
    int qToc1 = 4 * QUARTERLY_TOC_PER_GAME;
    Game game1 = Game.builder()
        .quarterlyTocCollected(qToc1)
        .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(10);
    for (int i = 0; i < 10; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .qTocPoints(i < 4 ? i : 0)
          .quarterlyTocParticipant(i < 4)
          .build();
      gamePlayers.add(gamePlayer);
    }
    game1.setPlayers(gamePlayers);

    // Game 2
    // 7 quarterly toc
    int qToc2 = 7 * QUARTERLY_TOC_PER_GAME;

    Game game2 = Game.builder()
        .quarterlyTocCollected(qToc2)
        .build();

    gamePlayers = new ArrayList<>(16);
    for (int i = 0; i < 16; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .id(i)
          .playerId(i)
          .gameId(1)
          .qTocPoints(i < 7 ? i : 0)
          .quarterlyTocParticipant(i < 7)
          .build();
      gamePlayers.add(gamePlayer);
    }

    game2.setPlayers(gamePlayers);

    when(gameModule.getByQuarterlySeasonId(1)).thenReturn(Arrays.asList(game1, game2));

    // Act
    calculator.calculate(1);

    // Assert
    ArgumentCaptor<QuarterlySeason> qSeasonArg = ArgumentCaptor.forClass(QuarterlySeason.class);
    Mockito.verify(repository, Mockito.times(1)).save(qSeasonArg.capture());
    QuarterlySeason qSeason = qSeasonArg.getValue();

    assertEquals(1, qSeason.getId());
    assertEquals(2, qSeason.getNumGamesPlayed());
    assertEquals(qToc1 + qToc2, qSeason.getQTocCollected());

    assertEquals(2, qSeason.getNumGamesPlayed());

    Assert.assertTrue("last calculated should be within the last few seconds",
        qSeason.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    assertEquals(7, qSeason.getPlayers().size());
    // Quarterly Season players are sorted by points
    QuarterlySeasonPlayer qSeasonPlayer = qSeason.getPlayers().get(0);
    assertEquals(1, qSeasonPlayer.getPlace().intValue());
    assertEquals(6, qSeasonPlayer.getPoints());

    qSeasonPlayer = qSeason.getPlayers().get(1);
    assertEquals(1, qSeasonPlayer.getPlace().intValue());
    assertEquals(6, qSeasonPlayer.getPoints());

    qSeasonPlayer = qSeason.getPlayers().get(2);
    assertEquals(3, qSeasonPlayer.getPlace().intValue());
    assertEquals(5, qSeasonPlayer.getPoints());
    assertEquals(1, qSeasonPlayer.getEntries());

    qSeasonPlayer = qSeason.getPlayers().get(3);
    assertEquals(4, qSeasonPlayer.getPlace().intValue());
    assertEquals(4, qSeasonPlayer.getPoints());

    qSeasonPlayer = qSeason.getPlayers().get(4);
    assertEquals(4, qSeasonPlayer.getPlace().intValue());
    assertEquals(4, qSeasonPlayer.getPoints());

    qSeasonPlayer = qSeason.getPlayers().get(5);
    assertEquals(6, qSeasonPlayer.getPlace().intValue());
    assertEquals(2, qSeasonPlayer.getPoints());
    assertEquals(2, qSeasonPlayer.getEntries());

    qSeasonPlayer = qSeason.getPlayers().get(6);
    assertNull(qSeasonPlayer.getPlace());
    assertEquals(0, qSeasonPlayer.getPoints());

    // TODO beef up testing payouts
    assertEquals(3, qSeason.getPayouts().size());
  }

}
