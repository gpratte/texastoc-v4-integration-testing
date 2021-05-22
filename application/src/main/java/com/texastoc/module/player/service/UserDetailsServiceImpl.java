package com.texastoc.module.player.service;

import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.repository.PlayerRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final PlayerRepository playerRepository;

  public UserDetailsServiceImpl(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    List<Player> players = playerRepository.findByEmail(email);
    if (players.size() == 0) {
      throw new UsernameNotFoundException(email);
    }
    Player player = players.get(0);
    return new User(player.getEmail(), player.getPassword(), getAuthority(player));
  }

  private Set<SimpleGrantedAuthority> getAuthority(Player player) {
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    player.getRoles().forEach(role -> {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getType().name()));
    });
    return authorities;
  }

}
