package com.texastoc.module.game;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import java.util.List;

public interface GameModule {

  /**
   * Create a game. The only fields of the Game that are pertinent are
   * <ul>
   *   <li>hostId</li>
   *   <li>date</li>
   *   <li>transportRequired</li>
   *
   * </ul>
   * If transportRequired is not provided the default value is false.
   *
   * @param game     a game with the hostId, date and transportRequired fields set
   * @param seasonId the season to which the game will belong
   * @return the newly created game
   */
  Game create(Game game, Integer seasonId);

  /**
   * Update a game. The only fields of the Game that are pertinent are
   * <ul>
   *   <li>hostId</li>
   *   <li>date</li>
   *   <li>transportRequired</li>
   * </ul>
   * If transportRequired is not provided the default value is false.
   *
   * @param game a game with the hostId, date and transportRequired fields set
   * @throws NotFoundException
   */
  Game update(Game game);

  /**
   * Get a game
   *
   * @param id the game Id
   * @return the game
   * @throws NotFoundException
   */
  Game get(int id);

  /**
   * Get the games for the given season Id.
   *
   * @param seasonId the season Id
   * @return the games for the corresponding season
   */
  List<Game> getBySeasonId(Integer seasonId);

  /**
   * Get the games for the given player Id.
   *
   * @param playerId the player Id
   * @return the games for the corresponding player
   */
  List<Game> getByPlayerId(int playerId);

  /**
   * Get the games for the given quarterly season Id.
   *
   * @param qSeasonId the quarterly season Id
   * @return the games for the corresponding quarterly season
   */
  List<Game> getByQuarterlySeasonId(Integer qSeasonId);

  /**
   * Finalize (end) the game.
   *
   * @param id the game Id
   * @throws NotFoundException
   */
  Game finalize(int id);

  /**
   * Unfinalize (reopen) a game. Restricted to admins only.
   *
   * @param id the game Id
   * @throws NotFoundException
   */
  Game unfinalize(int id);

  /**
   * Create a game player. The only fields of the GamePlayer that are pertinent are
   * <ul>
   *   <li>playerId</li>
   *   <li>buyInCollected</li>
   *   <li>annualTocCollected</li>
   *   <li>quarterlyTocCollected</li>
   * </ul>
   *
   * @param gamePlayer a game player with at least playerId
   * @return the newly created game player
   * @throws NotFoundException
   */
  GamePlayer createGamePlayer(GamePlayer gamePlayer);

  /**
   * Create a first time game player. The only fields of the GdGamePlayer that are pertinent are
   * <ul>
   *   <li>firstName</li>
   *   <li>lastName</li>
   *   <li>email</li>
   *   <li>buyInCollected</li>
   *   <li>annualTocCollected</li>
   *   <li>quarterlyTocCollected</li>
   * </ul>
   *
   * @param gamePlayer a game player with at least either firstName or lastName set
   * @return the newly created first time game player
   * @throws NotFoundException
   */
  GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer);

  /**
   * Update the game player. The pertinent fields are
   * <ul>
   *   <li>place</li>
   *   <li>knockedOut</li>
   *   <li>roundUpdates</li>
   *   <li>buyInCollected</li>
   *   <li>rebuyAddOnCollected</li>
   *   <li>annualTocCollected</li>
   *   <li>quarterlyTocCollected</li>
   *   <li>chop</li>
   * </ul>
   *
   * @param gamePlayer the game player some or none of the pertinent fields
   * @throws NotFoundException
   */
  GamePlayer updateGamePlayer(GamePlayer gamePlayer);

  /**
   * Toggles the knocked out field of the game player
   *
   * @param gameId       the game Id
   * @param gamePlayerId the game player Id
   * @throws NotFoundException
   */
  GamePlayer toggleGamePlayerKnockedOut(int gameId, int gamePlayerId);

  /**
   * Toggles the rebuy field of the game player
   *
   * @param gameId       the game Id
   * @param gamePlayerId the game player Id
   * @throws NotFoundException
   */
  GamePlayer toggleGamePlayerRebuy(int gameId, int gamePlayerId);

  /**
   * Deletes the game player
   *
   * @param gameId       the game Id
   * @param gamePlayerId the game player Id
   * @throws NotFoundException
   */
  void deleteGamePlayer(int gameId, int gamePlayerId);

  /**
   * Set the seating for the game. The pertinent fields are
   * <ul>
   *   <li>numSeatsPerTable</li>
   *   <li>tableRequests</li>
   * </ul>
   *
   * @param seating the seating with the perinent fields
   * @return the seating with the tables
   * @throws NotFoundException
   */
  Seating seatGamePlayers(Seating seating);

  /**
   * Notify the game players as to where to find their seat
   *
   * @param gameId the game Id
   * @throws NotFoundException
   */
  void notifySeating(int gameId);

  /**
   * Update the <i>canRebuy</i> field of a game
   *
   * @param gameId the game Id
   * @param value  true or falsd
   */
  Game updateCanRebuy(int gameId, boolean value);
}
