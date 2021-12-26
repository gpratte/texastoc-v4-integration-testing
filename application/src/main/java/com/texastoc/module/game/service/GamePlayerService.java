package com.texastoc.module.game.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.exception.BLException;
import com.texastoc.exception.BLType;
import com.texastoc.exception.ErrorDetails;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GamePlayerService {

  private final GameRepository gameRepository;
  private final GameHelper gameHelper;

  private PlayerModule playerModule;

  public GamePlayerService(GameRepository gameRepository, GameHelper gameHelper) {
    this.gameRepository = gameRepository;
    this.gameHelper = gameHelper;
  }

  @Transactional
  public GamePlayer createGamePlayer(GamePlayer gamePlayer) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game game = gameHelper.get(gamePlayer.getGameId());
    gameHelper.checkFinalized(game);

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    gameHelper.recalculate(game.getId());
    gameHelper.sendUpdatedGame();
    return gamePlayerCreated;
  }

  @Transactional
  public GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game game = gameHelper.get(gamePlayer.getGameId());
    gameHelper.checkFinalized(game);

    String firstName = gamePlayer.getFirstName();
    String lastName = gamePlayer.getLastName();
    Player player = Player.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(gamePlayer.getEmail())
        .roles(ImmutableSet.of(Role.builder()
            .type(Role.Type.USER)
            .build()))
        .build();
    int playerId = getPlayerModule().create(player).getId();
    gamePlayer.setPlayerId(playerId);

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    gameHelper.recalculate(game.getId());
    gameHelper.sendUpdatedGame();
    return gamePlayerCreated;
  }

  @Transactional
  public GamePlayer updateGamePlayer(GamePlayer gamePlayer) {
    Game game = gameHelper.get(gamePlayer.getGameId());
    gameHelper.checkFinalized(game);

    GamePlayer existingGamePlayer = game.getPlayers().stream()
        .filter(gp -> gp.getId() == gamePlayer.getId())
        .findFirst().get();

    existingGamePlayer.setPlace(gamePlayer.getPlace());
    existingGamePlayer.setRoundUpdates(gamePlayer.isRoundUpdates());

    if (gamePlayer.isBoughtIn()) {
      existingGamePlayer.setBoughtIn(true);
      existingGamePlayer.setBuyInCollected(game.getBuyInCost());
    } else {
      existingGamePlayer.setBoughtIn(false);
      existingGamePlayer.setBuyInCollected(null);
    }

    if (gamePlayer.isRebought()) {
      existingGamePlayer.setRebought(true);
      existingGamePlayer.setRebuyAddOnCollected(game.getRebuyAddOnCost());
    } else {
      existingGamePlayer.setRebought(false);
      existingGamePlayer.setRebuyAddOnCollected(null);
    }

    if (gamePlayer.isAnnualTocParticipant()) {
      existingGamePlayer.setAnnualTocParticipant(true);
      existingGamePlayer.setAnnualTocCollected(game.getAnnualTocCost());
    } else {
      existingGamePlayer.setAnnualTocParticipant(false);
      existingGamePlayer.setAnnualTocCollected(null);
    }

    if (gamePlayer.isQuarterlyTocParticipant()) {
      existingGamePlayer.setQuarterlyTocParticipant(true);
      existingGamePlayer.setQuarterlyTocCollected(game.getQuarterlyTocCost());
    } else {
      existingGamePlayer.setQuarterlyTocParticipant(false);
      existingGamePlayer.setQuarterlyTocCollected(null);
    }

    existingGamePlayer.setChop(gamePlayer.getChop());

    if (gamePlayer.getPlace() != null && gamePlayer.getPlace() <= 10) {
      existingGamePlayer.setKnockedOut(true);
    } else {
      existingGamePlayer.setKnockedOut(gamePlayer.isKnockedOut());
    }

    gameRepository.save(game);
    gameHelper.recalculate(game.getId());
    gameHelper.sendUpdatedGame();
    return existingGamePlayer;
  }

  @Transactional
  public GamePlayer toggleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    Game game = gameHelper.get(gameId);
    gameHelper.checkFinalized(game);

    Optional<GamePlayer> optionalGamePlayer = game.getPlayers().stream()
        .filter(gp -> gp.getId() == gamePlayerId)
        .findFirst();
    if (!optionalGamePlayer.isPresent()) {
      throw new BLException(BLType.NOT_FOUND, ErrorDetails.builder()
          .target("gamePlayer")
          .message("with id '" + gamePlayerId + "' not found")
          .build());
    }
    GamePlayer gamePlayer = optionalGamePlayer.get();
    gamePlayer.setKnockedOut(!gamePlayer.isKnockedOut());
    gameRepository.save(game);
    gameHelper.sendUpdatedGame();
    return gamePlayer;
  }

  @Transactional
  public GamePlayer toggleGamePlayerRebuy(int gameId, int gamePlayerId) {
    Game game = gameHelper.get(gameId);
    gameHelper.checkFinalized(game);

    Optional<GamePlayer> optionalGamePlayer = game.getPlayers().stream()
        .filter(gp -> gp.getId() == gamePlayerId)
        .findFirst();
    if (!optionalGamePlayer.isPresent()) {
      throw new BLException(BLType.NOT_FOUND, ErrorDetails.builder()
          .target("gamePlayer")
          .message("with id '" + gamePlayerId + "' not found")
          .build());
    }
    GamePlayer gamePlayer = optionalGamePlayer.get();
    gamePlayer.setRebought(!gamePlayer.isRebought());
    gameRepository.save(game);
    gameHelper.recalculate(game.getId());
    gameHelper.sendUpdatedGame();
    return gamePlayer;
  }

  @Transactional
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    Game game = gameHelper.get(gameId);
    gameHelper.checkFinalized(game);

    if (game.getPlayers() != null) {
      // Remove the game player from the list of game players
      game.setPlayers(game.getPlayers().stream()
          .filter(gp -> gp.getId() != gamePlayerId)
          .collect(Collectors.toList()));
      gameRepository.save(game);
      gameHelper.recalculate(game.getId());
      gameHelper.sendUpdatedGame();
    }
  }

  private GamePlayer createGamePlayerWorker(GamePlayer gamePlayer, Game game) {
    if (gamePlayer.getFirstName() == null && gamePlayer.getLastName() == null) {
      // If the first and last name are not set then this is an existing
      // player so copy fields to the game player
      Player player = getPlayerModule().get(gamePlayer.getPlayerId());
      gamePlayer.setFirstName(player.getFirstName());
      gamePlayer.setLastName(player.getLastName());
      gamePlayer.setPhone(player.getPhone());
      gamePlayer.setEmail(player.getEmail());
    }
    gamePlayer.setQSeasonId(game.getQSeasonId());
    gamePlayer.setSeasonId(game.getSeasonId());

    if (gamePlayer.isBoughtIn()) {
      gamePlayer.setBuyInCollected(game.getBuyInCost());
    }

    if (gamePlayer.isRebought()) {
      gamePlayer.setRebuyAddOnCollected(game.getRebuyAddOnCost());
    }

    if (gamePlayer.isAnnualTocParticipant()) {
      gamePlayer.setAnnualTocCollected(game.getAnnualTocCost());
    }

    if (gamePlayer.isQuarterlyTocParticipant()) {
      gamePlayer.setQuarterlyTocCollected(game.getQuarterlyTocCost());
    }

    if (game.getPlayers() == null) {
      game.setPlayers(new ArrayList<>(1));
    }
    game.getPlayers().add(gamePlayer);
    gameRepository.save(game);
    return gamePlayer;
  }

  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

}
