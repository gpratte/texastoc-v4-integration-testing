package com.texastoc.module.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImplTest {

  private PlayerRepository playerRepository;
  private UserDetailsServiceImpl userDetailsServiceImpl;

  @Before
  public void init() {
    playerRepository = mock(PlayerRepository.class);
    userDetailsServiceImpl = new UserDetailsServiceImpl(playerRepository);
  }

  @Test
  public void testLoadUserByUsername() {
    // Arrange
    String email = "abc@def.com";
    String password = "password";
    List<Player> players = new LinkedList<>();
    players.add(Player.builder()
        .email(email)
        .password(password)
        .roles(ImmutableSet.of(
            Role.builder().type(Role.Type.ADMIN).build(),
            Role.builder().type(Role.Type.USER).build()))
        .build());
    when(playerRepository.findByEmail(email)).thenReturn(players);

    // Act
    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);

    // Assert
    assertThat(userDetails.getUsername()).isEqualTo(email);
    assertThat(userDetails.getPassword()).isEqualTo(password);

    SimpleGrantedAuthority adminGrant = new SimpleGrantedAuthority("ROLE_ADMIN");
    SimpleGrantedAuthority userGrant = new SimpleGrantedAuthority("ROLE_USER");
    Collection<? extends GrantedAuthority> grants = userDetails.getAuthorities();
    Assert.assertTrue(grants.contains(adminGrant));
    Assert.assertTrue(grants.contains(userGrant));
  }

  @Test
  public void testLoadUserByUsernameNotFound() {
    // Arrange
    String email = "abc@def.com";
    List<Player> players = new LinkedList<>();
    when(playerRepository.findByEmail(email)).thenReturn(players);

    // Act & Assert
    assertThatThrownBy(() -> {
      userDetailsServiceImpl.loadUserByUsername(email);
    }).isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("abc@def.com");
  }
}