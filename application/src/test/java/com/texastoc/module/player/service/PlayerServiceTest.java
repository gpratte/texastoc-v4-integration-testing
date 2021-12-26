package com.texastoc.module.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.texastoc.TestUtils;
import com.texastoc.common.AuthorizationHelper;
import com.texastoc.exception.BLException;
import com.texastoc.exception.BLType;
import com.texastoc.exception.ErrorDetails;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class PlayerServiceTest {

  private PlayerService playerService;
  private PlayerRepository playerRepository;
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  private AuthorizationHelper authorizationHelper;

  private GameModule gameModule;

  @Before
  public void before() {
    bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    playerRepository = mock(PlayerRepository.class);
    authorizationHelper = mock(AuthorizationHelper.class);
    playerService = new PlayerService(playerRepository, bCryptPasswordEncoder, authorizationHelper);

    gameModule = mock(GameModule.class);
    ReflectionTestUtils.setField(playerService, "gameModule", gameModule);
  }

  @Test
  public void testCreatePlayer() {
    // Arrange
    Player expected = Player.builder()
        .firstName("bobs")
        .lastName("youruncle")
        .phone("1234567890")
        .email("abc@xyz.com")
        .password("password")
        .build();

    when(playerRepository.save((Player) notNull())).thenReturn(Player.builder().id(1).build());

    // Act
    Player actual = playerService.create(expected);

    // Assert
    assertNotNull(actual);
    assertEquals(1, actual.getId());
    verify(playerRepository, Mockito.times(1)).save(any(Player.class));

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("bobs", param.getFirstName());
    assertEquals("youruncle", param.getLastName());
    assertEquals("1234567890", param.getPhone());
    assertEquals("abc@xyz.com", param.getEmail());
    assertThat(param.getRoles()).containsExactly(Role.builder()
        .type(Role.Type.USER)
        .build());
  }

  @Test
  public void testUpdateSelf() {
    testUpdate(Role.Type.USER);
  }

  @Test
  public void testUpdateAdmin() {
    testUpdate(Role.Type.ADMIN);
  }

  private void testUpdate(Role.Type roleType) {
    // Arrange
    Role existingRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    Player existingPlayer = Player.builder()
        .id(1)
        .firstName("existingFirstName")
        .lastName("existingLastName")
        .email("existing@xyz.com")
        .phone("existingPhone")
        .password("existingEncodedPassword")
        .roles(ImmutableSet.of(existingRole))
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    Player playersNewValues = Player.builder()
        .id(1)
        .firstName("updatedFirstName")
        .lastName("updatedLastName")
        .email("updated@xyz.com")
        .phone("updatedPhone")
        .password("updatedPassword") // will be ignored
        .roles(ImmutableSet.of(Role.builder() // will be ignored
            .type(Role.Type.ADMIN)
            .build()))
        .build();

    // mock out to pass the authorization check
    if (roleType == Role.Type.USER) {
      when(authorizationHelper.getLoggedInUserEmail()).thenReturn("existing@xyz.com");
      when(playerRepository.findByEmail("existing@xyz.com"))
          .thenReturn(ImmutableList.of(existingPlayer));
    } else {
      when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);
    }

    // Act
    playerService.update(playersNewValues);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
    Mockito.verify(bCryptPasswordEncoder, Mockito.times(0)).encode(any());
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("updatedFirstName", param.getFirstName());
    assertEquals("updatedLastName", param.getLastName());
    assertEquals("updatedPhone", param.getPhone());
    assertEquals("updated@xyz.com", param.getEmail());

    // roles should not change
    assertThat(param.getRoles()).containsExactly(existingRole);
  }

  @Test
  public void testUpdateNotAllowed() {
    // Arrange
    Role existingRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    Player existingPlayer = Player.builder()
        .id(1)
        .firstName("existingFirstName")
        .lastName("existingLastName")
        .email("existing@xyz.com")
        .phone("existingPhone")
        .password("existingEncodedPassword")
        .roles(ImmutableSet.of(existingRole))
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // different user
    Player differentPlayer = Player.builder()
        .id(2)
        .firstName("differentFirstName")
        .lastName("differentLastName")
        .email("different@xyz.com")
        .phone("differentPhone")
        .password("differentPassword")
        .roles(ImmutableSet.of(Role.builder() // will be ignored
            .type(Role.Type.ADMIN)
            .build()))
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(2)))
        .thenReturn(java.util.Optional.ofNullable(differentPlayer));

    // mock out authorization check
    when(authorizationHelper.getLoggedInUserEmail()).thenReturn("existing@xyz.com");
    when(playerRepository.findByEmail("existing@xyz.com"))
        .thenReturn(ImmutableList.of(existingPlayer));

    // Act & Assert ;;
    assertThatThrownBy(() -> {
      playerService.update(differentPlayer);
      ;
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.DENIED);
        });
  }

  @Test
  public void testGetAll() {
    // Arrange
    Player player2 = Player.builder()
        .id(1)
        .firstName("firstName2")
        .lastName("lastName2")
        .build();
    Player player1 = Player.builder()
        .id(1)
        .firstName("firstName1")
        .lastName("lastName1")
        .build();
    when(playerRepository.findAll()).thenReturn(ImmutableSet.of(player2, player1));

    // Act
    List<Player> players = playerService.getAll();

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findAll();

    // Should be sorted with player1 before player2
    assertThat(players).containsExactly(player1, player2);
  }

  @Test
  public void testGetNotFound() {
    // Arrange
    when(playerRepository.findById(123)).thenReturn(Optional.empty());

    // Act and Assert
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });
  }

  @Test
  public void testGetFound() {
    // Arrange
    Player player = Player.builder()
        .id(1)
        .firstName("firstName1")
        .lastName("lastName1")
        .email("player@email.com")
        .build();
    when(playerRepository.findById(1)).thenReturn(Optional.of(player));

    // mock out authorization check
    when(authorizationHelper.getLoggedInUserEmail()).thenReturn("player@email.com");
    when(playerRepository.findByEmail("player@email.com"))
        .thenReturn(ImmutableList.of(player));

    // Act
    Player playerRetrieved = playerService.get(1);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
  }

  @Test
  public void testGetByEmailNotFound() {
    // Arrange
    when(playerRepository.findByEmail(any())).thenReturn(Collections.emptyList());

    // Act and Assert
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });

    // Act and Assert
    assertThatThrownBy(() -> {
      playerService.getByEmail("abc");
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with email 'abc' not found")
              .build());
        });
  }

  @Test
  public void testGetByEmailFound() {
    // Arrange
    String email = "abc@def.com";
    Player player = Player.builder()
        .id(1)
        .firstName("firstName1")
        .lastName("lastName1")
        .email(email)
        .build();
    when(playerRepository.findByEmail(email)).thenReturn(ImmutableList.of(player));

    // Act
    Player playerRetrieved = playerService.getByEmail(email);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findByEmail(email);
    assertEquals(email, playerRetrieved.getEmail());
  }

  @Test
  public void testDeleteByAdmin() {
    // Arrange
    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);
    // mock out no games for the player
    when(gameModule.getByPlayerId(1)).thenReturn(Collections.emptyList());

    // Act
    playerService.delete(1);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).deleteById(1);
  }

  @Test
  public void testCannotDeletePlayer() {
    // Arrange
    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);
    // mock out one game for the player
    when(gameModule.getByPlayerId(1)).thenReturn(Collections.singletonList(Game.builder().build()));

    // Act & Assert
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });
    assertThatThrownBy(() -> {
      playerService.delete(1);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.CONFLICT, ErrorDetails.builder()
              .target("player")
              .message("1 cannot be deleted")
              .build());
        });
  }

  @Test
  public void testDeleteByNonAdmin() {
    // Arrange
    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(false);

    // Act
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });
    assertThatThrownBy(() -> {
      playerService.delete(1);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.DENIED);
        });
  }

  @Test
  public void testForgotPassword() {
    // Arrange
    NotificationModule notificationModule = mock(NotificationModule.class);
    ReflectionTestUtils.setField(playerService, "notificationModule", notificationModule);

    // Act
    playerService.forgotPassword("abc@def.com");

    // Assert
    Mockito.verify(notificationModule, Mockito.times(1)).sendEmail(any(), anyString(), anyString());
  }

  @Test
  public void testResetPassword() {
    // Arrange
    String email = "abc@def.com";
    String password = "newPassword";
    NotificationModule notificationModule = mock(NotificationModule.class);
    ReflectionTestUtils.setField(playerService, "notificationModule", notificationModule);

    // Act
    playerService.forgotPassword(email);

    // Assert
    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(notificationModule).sendEmail(any(), anyString(), argument.capture());
    String code = argument.getValue();
    System.out.println(code);

    Player player = Player.builder()
        .id(1)
        .firstName("firstName1")
        .lastName("lastName1")
        .email(email)
        .build();
    when(playerRepository.findByEmail(email)).thenReturn(ImmutableList.of(player));

    // Act
    playerService.resetPassword(code, password);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findByEmail(email);
    Mockito.verify(bCryptPasswordEncoder, Mockito.times(1)).encode(password);
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));
  }

  @Test
  public void testResetPasswordNotFound() {
    // Arrange
    String email = "abc@def.com";
    String password = "newPassword";
    NotificationModule notificationModule = mock(NotificationModule.class);
    ReflectionTestUtils.setField(playerService, "notificationModule", notificationModule);

    // Act
    playerService.forgotPassword(email);
    playerService.forgotPassword("another@whatever.com");

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(notificationModule, times(2)).sendEmail(any(), anyString(), argument.capture());
    String code = argument.getAllValues().get(0);

    Player player = Player.builder()
        .id(1)
        .firstName("firstName1")
        .lastName("lastName1")
        .email(email)
        .build();
    when(playerRepository.findByEmail(email)).thenReturn(ImmutableList.of(player));

    // Act
    playerService.resetPassword(code, password);

    // Cannot use the same code twice
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });
    assertThatThrownBy(() -> {
      playerService.resetPassword(code, password);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("code")
              .message("not found")
              .build());
        });
  }

  @Test
  public void testAddRole() {
    // Arrange
    Role existingRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    HashSet<Role> roles = new HashSet<>();
    roles.add(existingRole);
    Player existingPlayer = Player.builder()
        .id(1)
        .firstName("existingFirstName")
        .lastName("existingLastName")
        .email("existing@xyz.com")
        .phone("existingPhone")
        .password("existingEncodedPassword")
        .roles(roles)
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);

    Role newRole = Role.builder()
        .id(1)
        .type(Role.Type.ADMIN)
        .build();

    // Act
    playerService.addRole(1, newRole);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("existingFirstName", param.getFirstName());
    assertEquals("existingLastName", param.getLastName());
    assertEquals("existingPhone", param.getPhone());
    assertEquals("existing@xyz.com", param.getEmail());

    // password should have change
    Assert.assertNotEquals("existingEncodedPassword", param.getPassword());
    // roles should have change
    assertThat(param.getRoles()).containsExactlyInAnyOrder(existingRole, newRole);
  }

  @Test
  public void testAddRoleThatExists() {
    // Arrange
    Role existingRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    HashSet<Role> roles = new HashSet<>();
    roles.add(existingRole);
    Player existingPlayer = Player.builder()
        .id(1)
        .roles(roles)
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);

    Role newRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();

    // Act
    playerService.addRole(1, newRole);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
    Mockito.verify(playerRepository, Mockito.times(0)).save(any(Player.class));
  }

  @Test
  public void testRemoveRole() {
    // Arrange
    Role existingUserRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    Role existingAdminRole = Role.builder()
        .id(2)
        .type(Role.Type.ADMIN)
        .build();
    HashSet<Role> roles = new HashSet<>();
    roles.add(existingUserRole);
    roles.add(existingAdminRole);

    Player existingPlayer = Player.builder()
        .id(1)
        .firstName("existingFirstName")
        .lastName("existingLastName")
        .email("existing@xyz.com")
        .phone("existingPhone")
        .password("existingEncodedPassword")
        .roles(roles)
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);

    // Act, remove the admin role
    playerService.removeRole(1, 2);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("existingFirstName", param.getFirstName());
    assertEquals("existingLastName", param.getLastName());
    assertEquals("existingPhone", param.getPhone());
    assertEquals("existing@xyz.com", param.getEmail());

    // password should have change
    Assert.assertNotEquals("existingEncodedPassword", param.getPassword());
    // roles should have change
    assertThat(param.getRoles()).containsExactly(existingUserRole);
  }

  @Test
  public void testRemoveUnknownRole() {
    // Arrange
    Role existingUserRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    HashSet<Role> roles = new HashSet<>();
    roles.add(existingUserRole);

    Player existingPlayer = Player.builder()
        .id(1)
        .firstName("existingFirstName")
        .lastName("existingLastName")
        .email("existing@xyz.com")
        .phone("existingPhone")
        .password("existingEncodedPassword")
        .roles(roles)
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);

    // Act, remove the admin role
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });
    assertThatThrownBy(() -> {
      playerService.removeRole(1, 2);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("role")
              .message("with id '2' not found")
              .build());
        });
  }

  @Test
  public void testRemoveLastRole() {
    // Arrange
    Role existingUserRole = Role.builder()
        .id(1)
        .type(Role.Type.USER)
        .build();
    HashSet<Role> roles = new HashSet<>();
    roles.add(existingUserRole);

    Player existingPlayer = Player.builder()
        .id(1)
        .firstName("existingFirstName")
        .lastName("existingLastName")
        .email("existing@xyz.com")
        .phone("existingPhone")
        .password("existingEncodedPassword")
        .roles(roles)
        .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1)))
        .thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)).thenReturn(true);

    // Act, remove the admin role
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.NOT_FOUND, ErrorDetails.builder()
              .target("player")
              .message("with id '123' not found")
              .build());
        });
    assertThatThrownBy(() -> {
      playerService.removeRole(1, 1);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.BAD_REQUEST, ErrorDetails.builder()
              .target("player.roles")
              .message("cannot remove the last role")
              .build());
        });
  }
}
