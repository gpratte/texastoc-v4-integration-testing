package com.texastoc.module.game;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.service.GamePlayerService;
import com.texastoc.module.game.service.GameService;
import com.texastoc.module.game.service.SeatingService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GameModuleImpl implements GameModule {

  private final GameService gameService;
  private final GamePlayerService gamePlayerService;
  private final SeatingService seatingService;

  public GameModuleImpl(GameService gameService, GamePlayerService gamePlayerService,
      SeatingService seatingService) {
    this.gameService = gameService;
    this.gamePlayerService = gamePlayerService;
    this.seatingService = seatingService;
  }

  @Override
  public Game create(Game game, Integer seasonId) {
    return gameService.create(game, seasonId);
  }

  @Override
  public Game update(Game game) {
    return gameService.update(game);
  }

  @Override
  public Game get(int id) {
    return gameService.get(id);
  }

  @Override
  public List<Game> getBySeasonId(Integer seasonId) {
    return gameService.getBySeasonId(seasonId);
  }

  @Override
  public List<Game> getByPlayerId(int playerId) {
    return gameService.getByPlayerId(playerId);
  }

  @Override
  public List<Game> getByQuarterlySeasonId(Integer qSeasonId) {
    return gameService.getByQuarterlySeasonId(qSeasonId);
  }

  @Override
  public Game finalize(int id) {
    return gameService.finalize(id);
  }

  @Override
  public Game unfinalize(int id) {
    return gameService.unfinalize(id);
  }

  @Override
  public GamePlayer createGamePlayer(GamePlayer gamePlayer) {
    return gamePlayerService.createGamePlayer(gamePlayer);
  }

  @Override
  public GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer) {
    return gamePlayerService.createFirstTimeGamePlayer(gamePlayer);
  }

  @Override
  public GamePlayer updateGamePlayer(GamePlayer gamePlayer) {
    return gamePlayerService.updateGamePlayer(gamePlayer);
  }

  @Override
  public GamePlayer toggleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    return gamePlayerService.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @Override
  public GamePlayer toggleGamePlayerRebuy(int gameId, int gamePlayerId) {
    return gamePlayerService.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @Override
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    gamePlayerService.deleteGamePlayer(gameId, gamePlayerId);
  }

  @Override
  public Seating seatGamePlayers(Seating seating) {
    return seatingService.seatGamePlayers(seating);
  }

  @Override
  public void notifySeating(int gameId) {
    seatingService.notifySeating(gameId);
  }

  @Override
  public Game updateCanRebuy(int gameId, boolean value) {
    return gameService.updateCanRebuy(gameId, value);
  }
}
