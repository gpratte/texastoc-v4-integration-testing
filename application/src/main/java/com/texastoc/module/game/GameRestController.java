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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameRestController {

  public static final String CONTENT_TYPE_CURRENT_GAME = "application/vnd.texastoc.current+json";
  public static final String CONTENT_TYPE_CLEAR_CACHE = "application/vnd.texastoc.clear-cache+json";
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

  @PostMapping(value = "/api/v3/games", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Game createGame(@RequestBody Game game) {
    return gameModule.create(game);
  }

  @PatchMapping(value = "/api/v3/games/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Game updateGame(@PathVariable("id") int id, @RequestBody Game game) {
    game.setId(id);
    return gameModule.update(game);
  }

  @GetMapping("/api/v3/games/{id}")
  public Game getGame(@PathVariable("id") int id) {
    return gameModule.get(id);
  }

  @GetMapping(value = "/api/v3/games", consumes = CONTENT_TYPE_CURRENT_GAME)
  public Game getCurrentGame() {
    return gameModule.getCurrent();
  }

  // TODO this needs to go away
  @GetMapping(value = "/api/v3/games", consumes = CONTENT_TYPE_CLEAR_CACHE)
  public String getCurrentNoCacheGame() {
    gameService.clearCacheGame();
    return "done";
  }

  @GetMapping("/api/v3/games")
  public List<Game> getGamesBySeasonId(@RequestParam(required = false) Integer seasonId) {
    return gameModule.getBySeasonId(seasonId);
  }

  @PutMapping(value = "/api/v3/games/{id}", consumes = CONTENT_TYPE_FINALIZE)
  public Game finalizeGame(@PathVariable("id") int id) {
    return gameModule.finalize(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/api/v3/games/{id}", consumes = CONTENT_TYPE_UNFINALIZE)
  public Game unfinalizeGame(@PathVariable("id") int id) {
    return gameModule.unfinalize(id);
  }

  @PostMapping(value = "/api/v3/games/{id}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer createGamePlayer(@PathVariable("id") int id,
      @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setGameId(id);
    return gameModule.createGamePlayer(gamePlayer);
  }

  @PostMapping(value = "/api/v3/games/{id}/players", consumes = CONTENT_TYPE_NEW_GAME_PLAYER)
  public GamePlayer createFirstTimeGamePlayer(@PathVariable("id") int id,
      @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setGameId(id);
    return gameModule.createFirstTimeGamePlayer(gamePlayer);
  }

  @PatchMapping(value = "/api/v3/games/{gameId}/players/{gamePlayerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer updateGamePlayer(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId, @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setGameId(gameId);
    gamePlayer.setId(gamePlayerId);
    return gameModule.updateGamePlayer(gamePlayer);
  }

  @PutMapping(value = "/api/v3/games/{gameId}/players/{gamePlayerId}", consumes = CONTENT_TYPE_KNOCKOUT)
  public GamePlayer toggleKnockedOut(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId) {
    return gameModule.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @PutMapping(value = "/api/v3/games/{gameId}/players/{gamePlayerId}", consumes = CONTENT_TYPE_REBUY)
  public GamePlayer toggleRebuy(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId) {
    return gameModule.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @DeleteMapping("/api/v3/games/{gameId}/players/{gamePlayerId}")
  public void deleteGamePlayer(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId) {
    gameModule.deleteGamePlayer(gameId, gamePlayerId);
  }

  @PostMapping(value = "/api/v3/games/{gameId}/seats", consumes = CONTENT_TYPE_ASSIGN_SEATS)
  public Seating seating(@PathVariable("gameId") int gameId, @RequestBody Seating seating) {
    seating.setGameId(gameId);
    return gameModule.seatGamePlayers(seating);
  }

  @PostMapping(value = "/api/v3/games/{gameId}/seats", consumes = CONTENT_TYPE_NOTIFY_SEATING)
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
