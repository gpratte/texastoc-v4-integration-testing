package com.texastoc.module.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.TestUtils;
import com.texastoc.exception.BLException;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class PlayerIT extends BaseIntegrationTest {

  Player playerToCreate;
  Player anotherPlayerToCreate;
  Player playerCreated;
  Player anotherPlayerCreated;
  Player updatePlayer;
  Player playerRetrieved;
  List<Player> playersRetrieved;
  Exception exception;

  @Before
  public void before() {
    playerToCreate = null;
    anotherPlayerToCreate = null;
    playerCreated = null;
    anotherPlayerCreated = null;
    updatePlayer = null;
    playerRetrieved = null;
    playersRetrieved = null;
    exception = null;
  }

  @Test
  public void createAndGet() {
    // Arrange and Act
    newPlayer();
    // Assert
    getPlayer();
    thePlayerMatches();
  }

  @Test
  public void createMultipleAndGet() {
    newPlayer();
    anotherNewPlayer();
    getPlayers();
    thePlayersMatch();
  }

  @Test
  public void updatePlayerAsAdmin() {
    // An admin updates another player
    newPlayer();
    updatePlayer("admin");
    getPlayer();
    theUpdatePlayerMatches();
  }

  @Test
  public void updateAnotherPlayerAsNonAdmin() {
    //  A non-admin attempts to update another player
    newPlayer();
    updatePlayer("non-admin");
    checkForbidden();
  }

  @Test
  public void deletePlayerAsAdmin() {
    //  An admin deletes a player
    newPlayer();
    playerDeleted("admin");
    getPlayer();
    checkPlayerNotFound(playerCreated.getId());
  }

  @Test
  public void deletePlayerAsNonAdmin() {
    //  Delete player as non-admin
    newPlayer();
    playerDeleted("non-admin");
    getPlayer();
    checkForbidden();
  }

  public void addRoleAsAdmin() {
    //  Add role as admin
    newPlayer();
    addRole("admin");
    getPlayer();
    playerHasTwoRoles();
  }

  public void addRoleAsNonAdmin() {
    //  Add role as non-admin
    newPlayer();
    addRole("non-admin");
    getPlayer();
    checkForbidden();
  }

  public void removeRoleAsAdmin() {
    //  An admin adds and then removes a role
    newPlayer();
    addRole("admin");
    getPlayer();
    removeRole("admin");
    getPlayer();
    playerHasOneRole();
  }

  public void removeRoleAsNonAdmin() {
    // A non-admin attempts to remove a role
    newPlayer();
    addRole("admin");
    getPlayer();
    removeRole("non-admin");
    checkForbidden();
  }

  // Create a new player
  private void newPlayer() {
    playerToCreate = Player.builder()
        .firstName("John")
        .lastName("Luther")
        .email(UUID.randomUUID() + "@bbc.com")
        // 2732833 = created on the phone keyboard
        .phone("2732833")
        .password("password")
        .build();
    playerCreated = createPlayer(playerToCreate, login(ADMIN_EMAIL, ADMIN_PASSWORD));
  }

  // Another new player
  private void anotherNewPlayer() {
    anotherPlayerToCreate = Player.builder()
        .firstName("Jane")
        .lastName("Rain")
        .build();
    anotherPlayerCreated = createPlayer(anotherPlayerToCreate, login(ADMIN_EMAIL, ADMIN_PASSWORD));
  }

  // The (admin|non-admin) updates the player
  private void updatePlayer(String who) {
    updatePlayer = Player.builder()
        .id(playerCreated.getId())
        .firstName("updated_" + playerCreated.getFirstName())
        .lastName("updated_" + playerCreated.getLastName())
        .email("updated_" + playerCreated.getEmail())
        // 8732833 = updated on phone keyboard
        .phone("8732833z")
        .password("updated_" + "password")
        .build();

    String token = getToken(who);
    try {
      updatePlayer(updatePlayer, token);
    } catch (Exception e) {
      exception = e;
    }
  }

  // The (admin|non-admin) deletes the player
  private void playerDeleted(String who) {
    String token = getToken(who);
    try {
      deletePlayer(playerCreated.getId(), token);
    } catch (Exception e) {
      exception = e;
    }
  }

  // The player is retrieved
  private void getPlayer() {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    try {
      playerRetrieved = getPlayer(playerCreated.getId(), token);
    } catch (Exception e) {
      exception = e;
    }
  }

  // The (admin|non-admin) adds a role
  private void addRole(String who) {
    String token = getToken(who);
    try {
      addRole(playerCreated.getId(), Role.builder()
              .type(Role.Type.ADMIN)
              .build(),
          token);
    } catch (Exception e) {
      exception = e;
    }
  }

  // The (admin|non-admin) removes a role
  private void removeRole(String who) {
    String token = getToken(who);
    Role role = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false)
        .filter(r -> r.getType() == Role.Type.ADMIN)
        .findFirst().get();

    try {
      removeRole(playerCreated.getId(), role.getId(), token);
    } catch (Exception e) {
      exception = e;
    }
  }

  // The players are retrieved
  private void getPlayers() {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    playersRetrieved = getPlayers(token);
  }

  // The player matches
  private void thePlayerMatches() {
    playerMatches(playerToCreate, playerRetrieved);
  }

  // The updated player matches
  private void theUpdatePlayerMatches() {
    playerMatches(updatePlayer, playerRetrieved);
  }

  // The player has one role
  private void playerHasOneRole() {
    List<Role> roles = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false)
        .collect(Collectors.toList());
    assertEquals("should only have one role", 1, roles.size());
    assertEquals("should be USER role", Role.Type.USER, roles.get(0).getType());
  }

  // The player has two roles
  private void playerHasTwoRoles() {
    List<Role> roles = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false)
        .collect(Collectors.toList());
    assertEquals("should only have two roles", 2, roles.size());
    List<Role.Type> types = roles.stream().map(Role::getType).collect(Collectors.toList());
    assertThat(types).containsExactlyInAnyOrder(Role.Type.USER, Role.Type.ADMIN);
  }


  // A forbidden error happens
  private void checkForbidden() {
    assertTrue("exception should be HttpClientErrorException",
        (exception instanceof HttpClientErrorException));
    HttpClientErrorException e = (HttpClientErrorException) exception;
    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    BLException blException = TestUtils.convert(e);
    assertThat(blException).isNotNull();
    assertThat(blException.getCode()).isEqualTo("UNAUTHORIZED");
    assertThat(blException.getMessage()).isEqualTo("Denied");
  }

  // A not found error happens
  private void checkPlayerNotFound(int id) {
    assertTrue("exception should be HttpClientErrorException",
        (exception instanceof HttpClientErrorException));
    HttpClientErrorException e = (HttpClientErrorException) exception;
    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    BLException blException = TestUtils.convert(e);
    assertThat(blException).isNotNull();
    assertThat(blException.getCode()).isEqualTo("INVALID REQUEST");
    assertThat(blException.getMessage()).isEqualTo("Not found");
    assertThat(blException.getDetails().getTarget()).isEqualTo("player");
    assertThat(blException.getDetails().getMessage()).isEqualTo("with id '" + id + "' not found");
  }

  private void playerMatches(Player request, Player response) {
    assertEquals("first name should match", request.getFirstName(), response.getFirstName());
    assertEquals("last name should match", request.getLastName(), response.getLastName());
    assertEquals("phone should match", request.getPhone(), response.getPhone());
    assertEquals("email should match", request.getEmail(), response.getEmail());
    assertNull("password should be null", response.getPassword());
    assertEquals("should only have one role", 1, response.getRoles().size());

    List<Role> roles = StreamSupport.stream(response.getRoles().spliterator(), false)
        .collect(Collectors.toList());
    assertEquals("should only have one role", 1, roles.size());
    assertEquals("should be USER role", Role.Type.USER, roles.get(0).getType());
  }

  // The players match
  private void thePlayersMatch() {
    boolean firstMatch = false;
    for (Player player : playersRetrieved) {
      if (player.getId() == playerCreated.getId()) {
        assertEquals("first name should match", playerToCreate.getFirstName(),
            player.getFirstName());
        assertEquals("last name should match", playerToCreate.getLastName(), player.getLastName());
        firstMatch = true;
      }
    }
    assertTrue("should have returned the first player created", firstMatch);

    boolean secondMatch = false;
    for (Player player : playersRetrieved) {
      if (player.getId() == anotherPlayerCreated.getId()) {
        assertEquals("first name should match", anotherPlayerToCreate.getFirstName(),
            player.getFirstName());
        assertEquals("last name should match", anotherPlayerToCreate.getLastName(),
            player.getLastName());
        secondMatch = true;
      }
    }
    assertTrue("should have returned the second player created", secondMatch);
  }

  private String getToken(String who) {
    if ("admin".equals(who)) {
      return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    } else {
      return login(USER_EMAIL, USER_PASSWORD);
    }

  }
}
