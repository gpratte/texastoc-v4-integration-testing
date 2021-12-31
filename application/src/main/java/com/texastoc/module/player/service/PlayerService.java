package com.texastoc.module.player.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.common.AuthorizationHelper;
import com.texastoc.exception.BLException;
import com.texastoc.exception.ErrorDetail;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.notification.NotificationModuleFactory;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PlayerService implements PlayerModule {

  private final PlayerRepository playerRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final AuthorizationHelper authorizationHelper;

  private NotificationModule notificationModule;
  private GameModule gameModule;

  // Only one server so save the forgot password codes here
  private Map<String, String> forgotPasswordCodes = new HashMap<>();

  public PlayerService(PlayerRepository playerRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder, AuthorizationHelper authorizationHelper) {
    this.playerRepository = playerRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.authorizationHelper = authorizationHelper;
  }

  @Override
  @Transactional
  public Player create(Player player) {
    Player playerToCreate = Player.builder()
        .firstName(player.getFirstName())
        .lastName(player.getLastName())
        .email(player.getEmail())
        .phone(player.getPhone())
        .password(null)
        .roles(ImmutableSet.of(Role.builder()
            .type(Role.Type.USER)
            .build()))
        .build();

    return playerRepository.save(playerToCreate);
  }

  @Override
  @Transactional
  public Player update(Player player) {
    verifyLoggedInUserIsAdminOrSelf(player);
    Player existingPlayer = playerRepository.findById(player.getId()).get();
    player.setPassword(existingPlayer.getPassword());
    player.setRoles((existingPlayer.getRoles()));
    playerRepository.save(player);
    player.setPassword(null);
    return player;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Player> getAll() {
    // TODO set passwords to null and other PII
    List<Player> players = StreamSupport.stream(playerRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
    Collections.sort(players);
    return players;
  }

  @Override
  @Transactional(readOnly = true)
  public Player get(int id) {
    Optional<Player> optionalPlayer = playerRepository.findById(id);
    if (!optionalPlayer.isPresent()) {
      throw new BLException(HttpStatus.NOT_FOUND, List.of(ErrorDetail.builder()
          .target("player")
          .message("with id '" + id + "' not found")
          .build()));
    }
    Player player = optionalPlayer.get();
    player.setPassword(null);
    // TODO set other PII to null
    return player;
  }

  @Transactional(readOnly = true)
  public Player getByEmail(String email) {
    List<Player> players = playerRepository.findByEmail(email);
    if (players.size() != 1) {
      throw new BLException(HttpStatus.NOT_FOUND, List.of(ErrorDetail.builder()
          .target("player")
          .message("with email '" + email + "' not found")
          .build()));
    }
    Player player = players.get(0);
    player.setPassword(null);
    return player;
  }

  @Override
  @Transactional
  public void delete(int id) {
    verifyLoggedInUserIsAdmin();

    List<Game> games = getGameModule().getByPlayerId(id);
    if (games.size() > 0) {
      throw new BLException(HttpStatus.CONFLICT, List.of(ErrorDetail.builder()
          .target("player")
          .message(id + " cannot be deleted")
          .build()));
    }
    playerRepository.deleteById(id);
  }

  @Override
  public void forgotPassword(String email) {
    String generatedString = RandomStringUtils
        .random(5, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
    forgotPasswordCodes.put(email, generatedString);
    log.info("reset code: {}", generatedString);
    getNotificaionModule().sendEmail(Arrays.asList(email), "Reset Code", generatedString);
  }

  @Override
  public void resetPassword(String code, String password) {
    String email = null;
    for (Map.Entry<String, String> forgotCode : forgotPasswordCodes.entrySet()) {
      if (forgotCode.getValue().equals(code)) {
        email = forgotCode.getKey();
        break;
      }
    }

    if (email == null) {
      throw new BLException(HttpStatus.NOT_FOUND, List.of(ErrorDetail.builder()
          .target("code")
          .message("not found")
          .build()));
    }

    forgotPasswordCodes.remove(email);

    Player playerToUpdate = playerRepository.findByEmail(email).get(0);
    playerToUpdate.setPassword(bCryptPasswordEncoder.encode(password));

    playerRepository.save(playerToUpdate);
  }

  @Override
  public Player addRole(int id, Role role) {
    verifyLoggedInUserIsAdmin();
    Player existingPlayer = get(id);
    // Check that role is not already set
    for (Role existingRole : existingPlayer.getRoles()) {
      if (existingRole.getType() == role.getType()) {
        existingPlayer.setPassword(null);
        return existingPlayer;
      }
    }
    existingPlayer.getRoles().add(role);
    playerRepository.save(existingPlayer);
    existingPlayer.setPassword(null);
    return existingPlayer;
  }

  @Override
  public Player removeRole(int id, int roleId) {
    verifyLoggedInUserIsAdmin();
    Player existingPlayer = get(id);
    // Check that role is set
    boolean found = false;
    Set<Role> existingRoles = existingPlayer.getRoles();
    for (Role existingRole : existingRoles) {
      if (existingRole.getId() == roleId) {
        found = true;
        break;
      }
    }

    if (!found) {
      throw new BLException(HttpStatus.NOT_FOUND, List.of(ErrorDetail.builder()
          .target("role")
          .message("with id '" + roleId + "' not found")
          .build()));
    }

    // found the role, now make sure it is not the only role
    if (existingRoles.size() < 2) {
      throw new BLException(HttpStatus.BAD_REQUEST, List.of(ErrorDetail.builder()
          .target("player.roles")
          .message("cannot remove the last role")
          .build()));
    }

    Set<Role> newRoles = new HashSet<>();
    for (Role existingRole : existingRoles) {
      if (existingRole.getId() != roleId) {
        newRoles.add(existingRole);
      }
    }

    existingPlayer.setRoles(newRoles);
    playerRepository.save(existingPlayer);
    existingPlayer.setPassword(null);
    return existingPlayer;
  }

  // verify the user is an admin
  private void verifyLoggedInUserIsAdmin() {
    if (!authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)) {
      throw new BLException(HttpStatus.FORBIDDEN);
    }
  }

  // verify the user is either admin or acting upon itself
  private void verifyLoggedInUserIsAdminOrSelf(Player player) {
    if (!authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)) {
      String email = authorizationHelper.getLoggedInUserEmail();
      List<Player> players = playerRepository.findByEmail(email);
      if (players.size() != 1) {
        throw new BLException(HttpStatus.NOT_FOUND, List.of(ErrorDetail.builder()
            .target("player")
            .message("with email '" + email + "' not found")
            .build()));
      }
      Player loggedInPlayer = players.get(0);
      if (loggedInPlayer.getId() != player.getId()) {
        throw new BLException(HttpStatus.FORBIDDEN);
      }
    }
  }

  private NotificationModule getNotificaionModule() {
    if (notificationModule == null) {
      notificationModule = NotificationModuleFactory.getNotificationModule();
    }
    return notificationModule;
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }
}
