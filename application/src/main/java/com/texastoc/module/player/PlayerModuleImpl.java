package com.texastoc.module.player;

import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.service.PlayerService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlayerModuleImpl implements PlayerModule {

  private final PlayerService playerService;

  public PlayerModuleImpl(PlayerService playerService) {
    this.playerService = playerService;
  }

  @Override
  public Player create(Player player) {
    return playerService.create(player);
  }

  @Override
  public Player update(Player player) {
    return playerService.update(player);
  }

  @Override
  public Player addRole(int id, Role role) {
    return playerService.addRole(id, role);
  }

  @Override
  public Player removeRole(int id, int roleId) {
    return playerService.removeRole(id, roleId);
  }

  @Override
  public List<Player> getAll() {
    return playerService.getAll();
  }

  @Override
  public Player get(int id) {
    return playerService.get(id);
  }

  @Override
  public void delete(int id) {
    playerService.delete(id);
  }

  @Override
  public void forgotPassword(String email) {
    playerService.forgotPassword(email);
  }

  @Override
  public void resetPassword(String code, String password) {
    playerService.resetPassword(code, password);
  }
}
