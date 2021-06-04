package com.texastoc.module.game;

import com.texastoc.module.game.exception.GameInProgressException;
import com.texastoc.module.game.exception.GameIsFinalizedException;
import com.texastoc.module.game.exception.SeatingException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.service.GameService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class GameRestController {

  public static final String CONTENT_TYPE_FINALIZE = "application/vnd.texastoc.finalize+json";
  public static final String CONTENT_TYPE_UNFINALIZE = "application/vnd.texastoc.unfinalize+json";
  public static final String CONTENT_TYPE_NEW_GAME_PLAYER = "application/vnd.texastoc.first-time+json";
  public static final String CONTENT_TYPE_KNOCKOUT = "application/vnd.texastoc.knockout+json";
  public static final String CONTENT_TYPE_REBUY = "application/vnd.texastoc.rebuy+json";
  public static final String CONTENT_TYPE_ASSIGN_SEATS = "application/vnd.texastoc.assign-seats+json";
  public static final String CONTENT_TYPE_NOTIFY_SEATING = "application/vnd.texastoc.notify-seating+json";

  private final GameModule gameModule;
  private final GameService gameService;

  public GameRestController(GameModuleImpl gameModuleImpl, GameService gameService) {
    gameModule = gameModuleImpl;
    this.gameService = gameService;
  }

  // TODO need season path for all these endpoints
  @PostMapping(value = "/seasons/{seasonId}/games", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Game createGame(@PathVariable("seasonId") int seasonId, @RequestBody Game game) {
    return gameModule.create(game, seasonId);
  }

  @PatchMapping(value = "/games/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Game updateGame(@PathVariable("id") int id, @RequestBody Game game) {
    game.setId(id);
    return gameModule.update(game);
  }

  @GetMapping("/games/{id}")
  public Game getGame(@PathVariable("id") int id) {
    return gameModule.get(id);
  }

  @GetMapping("/seasons/{seasonId}/games")
  public List<Game> getGamesBySeasonId(@PathVariable("seasonId") int seasonId) {
    return gameModule.getBySeasonId(seasonId);
  }

  @PutMapping(value = "/games/{id}", consumes = CONTENT_TYPE_FINALIZE)
  public Game finalizeGame(@PathVariable("id") int id) {
    return gameModule.finalize(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/games/{id}", consumes = CONTENT_TYPE_UNFINALIZE)
  public Game unfinalizeGame(@PathVariable("id") int id) {
    return gameModule.unfinalize(id);
  }

  @PostMapping(value = "/games/{id}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer createGamePlayer(@PathVariable("id") int id,
      @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setGameId(id);
    return gameModule.createGamePlayer(gamePlayer);
  }

  @PostMapping(value = "/games/{id}/players", consumes = CONTENT_TYPE_NEW_GAME_PLAYER)
  public GamePlayer createFirstTimeGamePlayer(@PathVariable("id") int id,
      @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setGameId(id);
    return gameModule.createFirstTimeGamePlayer(gamePlayer);
  }

  @PatchMapping(value = "/games/{gameId}/players/{gamePlayerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer updateGamePlayer(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId, @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setGameId(gameId);
    gamePlayer.setId(gamePlayerId);
    return gameModule.updateGamePlayer(gamePlayer);
  }

  @PutMapping(value = "/games/{gameId}/players/{gamePlayerId}", consumes = CONTENT_TYPE_KNOCKOUT)
  public GamePlayer toggleKnockedOut(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId) {
    return gameModule.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @PutMapping(value = "/games/{gameId}/players/{gamePlayerId}", consumes = CONTENT_TYPE_REBUY)
  public GamePlayer toggleRebuy(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId) {
    return gameModule.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @DeleteMapping("/games/{gameId}/players/{gamePlayerId}")
  public void deleteGamePlayer(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId) {
    gameModule.deleteGamePlayer(gameId, gamePlayerId);
  }

  @PostMapping(value = "/games/{gameId}/seats", consumes = CONTENT_TYPE_ASSIGN_SEATS)
  public Seating seating(@PathVariable("gameId") int gameId, @RequestBody Seating seating) {
    seating.setGameId(gameId);
    return gameModule.seatGamePlayers(seating);
  }

  @PostMapping(value = "/games/{gameId}/seats", consumes = CONTENT_TYPE_NOTIFY_SEATING)
  public void notifySeating(@PathVariable("gameId") int gameId) {
    gameModule.notifySeating(gameId);
  }

  @ExceptionHandler(value = {GameInProgressException.class})
  protected void handleGameInProgressException(GameInProgressException ex,
      HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

  @ExceptionHandler(value = {GameIsFinalizedException.class})
  protected void handleFinalizedException(GameIsFinalizedException ex, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

  @ExceptionHandler(value = {SeatingException.class})
  protected void handleSeatingException(SeatingException ex, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
  }
}
