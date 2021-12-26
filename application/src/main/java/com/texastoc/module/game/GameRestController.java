package com.texastoc.module.game;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

  public GameRestController(GameModuleImpl gameModuleImpl) {
    gameModule = gameModuleImpl;
  }

  // TODO need season path for all these endpoints
  @PostMapping(value = "/seasons/{seasonId}/games", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public Game createGame(@PathVariable("seasonId") int seasonId, @RequestBody Game game,
      HttpServletRequest request) {
    return gameModule.create(game, seasonId);
  }

  @PatchMapping(value = "/games/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public Game updateGame(@PathVariable("id") int id, @RequestBody Game game,
      HttpServletRequest request) {
    game.setId(id);
    return gameModule.update(game);
  }

  @GetMapping("/games/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Game getGame(@PathVariable("id") int id, HttpServletRequest request) {
    return gameModule.get(id);
  }

  @GetMapping("/seasons/{seasonId}/games")
  @ResponseStatus(HttpStatus.OK)
  public List<Game> getGamesBySeasonId(@PathVariable("seasonId") int seasonId,
      HttpServletRequest request) {
    return gameModule.getBySeasonId(seasonId);
  }

  @PutMapping(value = "/games/{id}", consumes = CONTENT_TYPE_FINALIZE)
  @ResponseStatus(HttpStatus.OK)
  public Game finalizeGame(@PathVariable("id") int id, HttpServletRequest request) {
    return gameModule.finalize(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/games/{id}", consumes = CONTENT_TYPE_UNFINALIZE)
  @ResponseStatus(HttpStatus.OK)
  public Game unfinalizeGame(@PathVariable("id") int id, HttpServletRequest request) {
    return gameModule.unfinalize(id);
  }

  @PostMapping(value = "/games/{id}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public GamePlayer createGamePlayer(@PathVariable("id") int id,
      @RequestBody GamePlayer gamePlayer, HttpServletRequest request) {
    gamePlayer.setGameId(id);
    return gameModule.createGamePlayer(gamePlayer);
  }

  @PostMapping(value = "/games/{id}/players", consumes = CONTENT_TYPE_NEW_GAME_PLAYER)
  @ResponseStatus(HttpStatus.CREATED)
  public GamePlayer createFirstTimeGamePlayer(@PathVariable("id") int id,
      @RequestBody GamePlayer gamePlayer, HttpServletRequest request) {
    gamePlayer.setGameId(id);
    return gameModule.createFirstTimeGamePlayer(gamePlayer);
  }

  @PatchMapping(value = "/games/{gameId}/players/{gamePlayerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public GamePlayer updateGamePlayer(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId, @RequestBody GamePlayer gamePlayer,
      HttpServletRequest request) {
    gamePlayer.setGameId(gameId);
    gamePlayer.setId(gamePlayerId);
    return gameModule.updateGamePlayer(gamePlayer);
  }

  @PutMapping(value = "/games/{gameId}/players/{gamePlayerId}", consumes = CONTENT_TYPE_KNOCKOUT)
  @ResponseStatus(HttpStatus.OK)
  public GamePlayer toggleKnockedOut(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId, HttpServletRequest request) {
    return gameModule.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @PutMapping(value = "/games/{gameId}/players/{gamePlayerId}", consumes = CONTENT_TYPE_REBUY)
  @ResponseStatus(HttpStatus.OK)
  public GamePlayer toggleRebuy(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId, HttpServletRequest request) {
    return gameModule.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @DeleteMapping("/games/{gameId}/players/{gamePlayerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGamePlayer(@PathVariable("gameId") int gameId,
      @PathVariable("gamePlayerId") int gamePlayerId, HttpServletRequest request) {
    gameModule.deleteGamePlayer(gameId, gamePlayerId);
  }

  @PostMapping(value = "/games/{gameId}/seats", consumes = CONTENT_TYPE_ASSIGN_SEATS)
  @ResponseStatus(HttpStatus.OK)
  public Seating seating(@PathVariable("gameId") int gameId, @RequestBody Seating seating,
      HttpServletRequest request) {
    seating.setGameId(gameId);
    return gameModule.seatGamePlayers(seating);
  }

  @PostMapping(value = "/games/{gameId}/seats", consumes = CONTENT_TYPE_NOTIFY_SEATING)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void notifySeating(@PathVariable("gameId") int gameId, HttpServletRequest request) {
    gameModule.notifySeating(gameId);
  }
}
