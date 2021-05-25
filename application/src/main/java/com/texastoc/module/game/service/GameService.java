package com.texastoc.module.game.service;

import com.texastoc.module.game.event.GameEventProducer;
import com.texastoc.module.game.exception.GameInProgressException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
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
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final GameHelper gameHelper;
  private final GameEventProducer gameEventProducer;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  public GameService(GameRepository gameRepository, GameHelper gameHelper,
      GameEventProducer gameEventProducer) {
    this.gameRepository = gameRepository;
    this.gameHelper = gameHelper;
    this.gameEventProducer = gameEventProducer;
  }

  @Transactional
  public Game create(Game game, int seasonId) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Season currentSeason = getSeasonModule().get(seasonId);

    // TODO check that date is allowed - not before an existing game and not beyond the season.

    // Make sure no other game is open
    List<Game> otherGames = gameRepository.findBySeasonId(currentSeason.getId());
    for (Game otherGame : otherGames) {
      if (!otherGame.isFinalized()) {
        throw new GameInProgressException();
      }
    }

    Player player = getPlayerModule().get(game.getHostId());
    game.setHostName(player.getName());

    // Game setup variables
    game.setSeasonId(currentSeason.getId());
    game.setKittyCost(currentSeason.getKittyPerGameCost());
    game.setBuyInCost(currentSeason.getBuyInCost());
    game.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    game.setRebuyAddOnTocDebitCost(currentSeason.getRebuyAddOnTocDebitCost());
    game.setAnnualTocCost(currentSeason.getTocPerGameCost());
    game.setQuarterlyTocCost(currentSeason.getQuarterlyTocPerGameCost());
    game.setSeasonGameNum(currentSeason.getNumGamesPlayed() + 1);

    game.setBuyInCost(currentSeason.getBuyInCost());
    game.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    game.setRebuyAddOnTocDebitCost(currentSeason.getRebuyAddOnTocDebitCost());

    QuarterlySeason currentQSeason = getQuarterlySeasonModule().getByDate(game.getDate());
    game.setQSeasonId(currentQSeason.getId());
    game.setQuarter(currentQSeason.getQuarter());
    game.setQuarterlyGameNum(currentQSeason.getNumGamesPlayed() + 1);

    game = gameRepository.save(game);

    gameHelper.sendUpdatedGame();
    return game;
  }

  @Transactional
  public Game update(Game game) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game currentGame = get(game.getId());
    gameHelper.checkFinalized(currentGame);
    currentGame.setHostId(game.getHostId());
    currentGame.setDate(game.getDate());
    currentGame.setTransportRequired(game.isTransportRequired());
    gameRepository.save(currentGame);
    if (currentGame.getPlayers() != null) {
      Collections.sort(currentGame.getPlayers());
    }
    gameHelper.sendUpdatedGame();
    return currentGame;
  }


  @Transactional
  public Game updateCanRebuy(int id, boolean value) {
    Game game = get(id);
    game.setCanRebuy(value);
    gameRepository.save(game);
    if (game.getPlayers() != null) {
      Collections.sort(game.getPlayers());
    }
    return game;
  }

  @Transactional(readOnly = true)
  public Game get(int id) {
    Game game = gameHelper.get(id);
    if (game.getPlayers() != null) {
      Collections.sort(game.getPlayers());
    }
    return game;
  }

  @Transactional(readOnly = true)
  public List<Game> getBySeasonId(Integer seasonId) {
    Assert.notNull(seasonId, "season Id required");

    List<Game> games = gameRepository.findBySeasonId(seasonId);
    games.forEach(game -> {
      List<GamePlayer> gamePlayers = game.getPlayers();
      if (gamePlayers != null) {
        Collections.sort(gamePlayers);
        game.setPlayers(gamePlayers);
      }
    });
    return games;
  }

  @Transactional(readOnly = true)
  public List<Game> getByPlayerId(int playerId) {
    List<Game> games = gameRepository.findByPlayerId(playerId);
    games.forEach(game -> {
      List<GamePlayer> gamePlayers = game.getPlayers();
      if (gamePlayers != null) {
        Collections.sort(gamePlayers);
        game.setPlayers(gamePlayers);
      }
    });
    return games;
  }

  @Transactional(readOnly = true)
  public List<Game> getByQuarterlySeasonId(Integer qSeasonId) {
    List<Game> games = gameRepository.findByQuarterlySeasonId(qSeasonId);
    games.forEach(game -> {
      List<GamePlayer> gamePlayers = game.getPlayers();
      if (gamePlayers != null) {
        Collections.sort(gamePlayers);
        game.setPlayers(gamePlayers);
      }
    });
    return games;
  }

  @CacheEvict(value = {"seasonById", "allSeasons"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public Game finalize(int id) {
    // TODO check that the game has the appropriate finishes (e.g. 1st, 2nd, ...)
    Game game = get(id);

    if (game.isFinalized()) {
      if (game.getPlayers() != null) {
        Collections.sort(game.getPlayers());
      }
      return game;
    }

    gameHelper.recalculate(game.getId());
    game = get(id);
    game.setFinalized(true);
    game.setSeating(null);
    gameRepository.save(game);
    if (game.getPlayers() != null) {
      Collections.sort(game.getPlayers());
    }
    gameEventProducer.notifyGameFinalized(id, game.getSeasonId(), game.getQSeasonId(), true);
    gameHelper.sendUpdatedGame();
    // TODO message clock to end
    gameHelper.sendGameSummary(id);
    return game;
  }

  @CacheEvict(value = {"seasonById", "allSeasons"}, allEntries = true, beforeInvocation = false)
  public Game unfinalize(int id) {
    Game gameToOpen = get(id);

    if (!gameToOpen.isFinalized()) {
      if (gameToOpen.getPlayers() != null) {
        Collections.sort(gameToOpen.getPlayers());
      }
      return gameToOpen;
    }

    Season season = getSeasonModule().get(gameToOpen.getSeasonId());
    if (season.isFinalized()) {
      // TODO throw a unique exception and handle in controller
      throw new RuntimeException("Cannot open a game when season is finalized");
    }

    // Make sure no other game is open
    List<Game> games = gameRepository.findBySeasonId(gameToOpen.getSeasonId());
    for (Game game : games) {
      if (game.getId() == gameToOpen.getId()) {
        continue;
      }
      if (!game.isFinalized()) {
        throw new GameInProgressException();
      }
    }

    gameToOpen.setFinalized(false);
    gameRepository.save(gameToOpen);
    if (gameToOpen.getPlayers() != null) {
      Collections.sort(gameToOpen.getPlayers());
    }
    gameHelper.sendUpdatedGame();
    return gameToOpen;
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
}
