package com.texastoc.module.game.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.texastoc.TestUtils;
import com.texastoc.exception.BLException;
import com.texastoc.exception.ErrorDetail;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.model.Season;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

public class GameHelperTest {

  private GameHelper gameHelper;
  private GameRepository gameRepository;
  private SeasonModule seasonModule;
  //  private WebSocketConnector webSocketConnector;
  private final GameCalculator gameCalculator = mock(GameCalculator.class);
  private final PayoutCalculator payoutCalculator = mock(PayoutCalculator.class);
  private final PointsCalculator pointsCalculator = mock(PointsCalculator.class);

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    PlayerModule playerModule = mock(PlayerModule.class);
    seasonModule = mock(SeasonModule.class);
//    webSocketConnector = mock(WebSocketConnector.class);
    gameHelper = new GameHelper(gameRepository, gameCalculator, payoutCalculator, pointsCalculator);
    ReflectionTestUtils.setField(gameHelper, "playerModule", playerModule);
    ReflectionTestUtils.setField(gameHelper, "seasonModule", seasonModule);
  }

  @Test
  public void testGetGame() {
    // Arrange
    LocalDate now = LocalDate.now();
    Game game = Game.builder()
        .id(111)
        .date(now)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    Game actual = gameHelper.get(111);

    // Assert
    verify(gameRepository, Mockito.times(1)).findById(111);
    assertEquals(111, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void testGetGameNotFound() {
    // Arrange
    when(gameRepository.findById(111)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> {
      gameHelper.get(111);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, HttpStatus.NOT_FOUND,
              List.of(ErrorDetail.builder()
                  .target("game")
                  .message("with id '111' not found")
                  .build()));
        });
  }

  @Test
  public void testCheckNotFinalized() {
    // Arrange
    LocalDate now = LocalDate.now();
    Game game = Game.builder()
        .finalized(false)
        .build();

    // Act
    // No exception should be thrown
    gameHelper.checkFinalized(game);
  }

  @Test
  public void testCheckFinalized() {
    // Arrange
    LocalDate now = LocalDate.now();
    Game game = Game.builder()
        .id(1)
        .finalized(true)
        .build();

    // Act & Assert
    assertThatThrownBy(() -> {
      gameHelper.checkFinalized(game);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, HttpStatus.CONFLICT,
              List.of(ErrorDetail.builder()
                  .target("game")
                  .message("1 is not finalized")
                  .build()));
        });
  }

  @Test
  public void testRecalculate() {
    // Arrange
    Game game = Game.builder()
        .id(111)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    Game calculatedGame = new Game();
    when(gameCalculator.calculate(game)).thenReturn(calculatedGame);

    // Act
    gameHelper.recalculate(game.getId());

    // Assert
    verify(gameRepository, Mockito.times(3)).findById(111);
    verify(gameCalculator, Mockito.times(1)).calculate(any());
    verify(payoutCalculator, Mockito.times(1)).calculate(any());
    verify(pointsCalculator, Mockito.times(1)).calculate(any());
  }

  // TODO
  @Ignore
  @Test
  public void testSendUpdate() throws InterruptedException {
    // Arrange (same as the getCurrent test above)
    when(seasonModule.get(2)).thenReturn(Season.builder()
        .id(2)
        .build());

    LocalDate now = LocalDate.now();
    Game game = Game.builder()
        .id(111)
        .date(now)
        .build();
    when(gameRepository.findUnfinalizedBySeasonId(2)).thenReturn(Collections.singletonList(111));
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    gameHelper.sendUpdatedGame();

    // Assert
    // Since this is a thread call, sleep for a half a second first
//    Thread.sleep(500l);
//    verify(webSocketConnector, Mockito.times(1)).sendGame(any(Game.class));
  }
}
