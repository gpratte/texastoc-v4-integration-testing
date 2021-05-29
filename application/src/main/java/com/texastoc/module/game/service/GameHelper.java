package com.texastoc.module.game.service;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.exception.GameIsFinalizedException;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
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

  private final WebSocketConnector webSocketConnector;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  public GameHelper(GameRepository gameRepository, GameCalculator gameCalculator,
      PayoutCalculator payoutCalculator, PointsCalculator pointsCalculator,
      WebSocketConnector webSocketConnector) {
    this.gameRepository = gameRepository;
    this.gameCalculator = gameCalculator;
    this.payoutCalculator = payoutCalculator;
    this.pointsCalculator = pointsCalculator;
    this.webSocketConnector = webSocketConnector;

    executorService = Executors.newCachedThreadPool();
  }

  @Transactional(readOnly = true)
  public Game get(int id) {
    Optional<Game> optionalGame = gameRepository.findById(id);
    if (!optionalGame.isPresent()) {
      throw new NotFoundException("Game with id " + id + " not found");
    }
    return optionalGame.get();
  }

  public void checkFinalized(Game game) {
    if (game.isFinalized()) {
      throw new GameIsFinalizedException();
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

  public void sendGameSummary(int id) {
    Game game = get(id);
    Season season = getSeasonModule().get(game.getSeasonId());
    List<Player> players = getPlayerModule().getAll();
    List<QuarterlySeason> quarterlySeasons = getQuarterlySeasonModule()
        .getBySeasonId(season.getId());
    GameSummary gameSummary = new GameSummary(game, season, quarterlySeasons, players);
    // TODO use Spring Integration
    new Thread(gameSummary).start();
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
}
