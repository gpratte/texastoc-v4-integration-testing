package com.texastoc.module.game.service;

import com.texastoc.exception.BLException;
import com.texastoc.exception.ErrorDetails;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.QuarterlySeasonModuleFactory;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.SeasonModuleFactory;
import com.texastoc.module.season.model.Season;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GameHelper {

  private final GameRepository gameRepository;
  private final GameCalculator gameCalculator;
  private final PayoutCalculator payoutCalculator;
  private final PointsCalculator pointsCalculator;
  private final ExecutorService executorService;

  //private final WebSocketConnector webSocketConnector;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  public GameHelper(GameRepository gameRepository, GameCalculator gameCalculator,
      PayoutCalculator payoutCalculator, PointsCalculator pointsCalculator) {
    this.gameRepository = gameRepository;
    this.gameCalculator = gameCalculator;
    this.payoutCalculator = payoutCalculator;
    this.pointsCalculator = pointsCalculator;

    executorService = Executors.newCachedThreadPool();
  }

  @Transactional(readOnly = true)
  public Game get(int id) {
    Optional<Game> optionalGame = gameRepository.findById(id);
    if (!optionalGame.isPresent()) {
      throw new BLException(HttpStatus.NOT_FOUND, ErrorDetails.builder()
          .target("game")
          .message("with id '" + id + "' not found")
          .build());
    }
    return optionalGame.get();
  }

  public void checkFinalized(Game game) {
    if (game.isFinalized()) {
      throw new BLException(HttpStatus.CONFLICT, ErrorDetails.builder()
          .target("game")
          .message(game.getId() + " is not finalized")
          .build());
    }
  }

  // TODO separate thread
  public void recalculate(int gameId) {
    Game game = get(gameId);
    gameCalculator.calculate(game);
    game = get(gameId);
    payoutCalculator.calculate(game);
    game = get(gameId);
    pointsCalculator.calculate(game);
  }

  // TODO use Spring Integration
  public void sendUpdatedGame() {
    executorService.submit(new GameSender());
  }

  public void sendGameSummary(int id, int seasonGameNum) {
    executorService.submit(new GameOver(id, seasonGameNum));
  }

  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

  private SeasonModule getSeasonModule() {
    if (seasonModule == null) {
      seasonModule = SeasonModuleFactory.getSeasonModule();
    }
    return seasonModule;
  }

  private QuarterlySeasonModule getQuarterlySeasonModule() {
    if (quarterlySeasonModule == null) {
      quarterlySeasonModule = QuarterlySeasonModuleFactory.getQuarterlySeasonModule();
    }
    return quarterlySeasonModule;
  }

  // TODO use Spring Integration
  private class GameSender implements Callable<Void> {

    @Override
    public Void call() throws Exception {
      // TODO
      //webSocketConnector.sendGame(getCurrent());
      return null;
    }
  }

  private class GameOver implements Callable<Void> {

    private final int gameId;
    private final int seasonGameNum;

    public GameOver(int gameId, int seasonGameNum) {
      this.gameId = gameId;
      this.seasonGameNum = seasonGameNum;
    }

    @Override
    public Void call() throws Exception {
      Game game = get(gameId);
      int seasonNumGamesPlayed = 0;
      Season season = null;
      // Wait for the season calculator to finish
      // TODO change to check the season's lastCalculated and the quarterlySeason's
      //  lastCalculated is after game's lastCalculated.
      // TODO why did I need to add now.isBefore(begin.plusMinutes(1))?
      LocalDateTime begin = LocalDateTime.now();
      LocalDateTime now = LocalDateTime.now();
      do {
        try {
          Thread.sleep(1000l);
        } catch (InterruptedException e) {
          // do nothing
        }
        season = getSeasonModule().get(game.getSeasonId());
        seasonNumGamesPlayed = season.getNumGamesPlayed();

        // Quit after a minute
        now = LocalDateTime.now();
      } while (seasonNumGamesPlayed != seasonGameNum && now.isBefore(begin.plusMinutes(1)));

      List<Player> players = getPlayerModule().getAll();
      List<QuarterlySeason> quarterlySeasons = getQuarterlySeasonModule()
          .getBySeasonId(season.getId());
      GameSummary gameSummary = new GameSummary(game, season, quarterlySeasons, players);
      // TODO use Spring Integration
      new Thread(gameSummary).start();
      return null;
    }
  }

}
