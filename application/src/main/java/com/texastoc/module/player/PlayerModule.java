package com.texastoc.module.player;

import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import java.util.List;

public interface PlayerModule {

  /**
   * Create a new player
   *
   * @param player
   * @return
   */
  Player create(Player player);

  /**
   * Update a player. Any player can update itself but only an admin can update another player. Only
   * the following fields can be updated
   * <ul>
   *   <li>firstName</li>
   *   <li>lastName</li>
   *   <li>phone</li>
   *   <li>email</li>
   * </ul>
   *
   * @param player
   */
  Player update(Player player);

  /**
   * Add a role to a player. Restricted to admins only.
   *
   * @param id   the player's Id
   * @param role the role to add
   */
  Player addRole(int id, Role role);

  /**
   * Remove a role from a player. Restricted to admins only.
   *
   * @param id     the player's Id
   * @param roleId the Id of role to remove
   */
  Player removeRole(int id, int roleId);

  /**
   * Get all players
   *
   * @return
   */
  List<Player> getAll();

  /**
   * Get a single player
   *
   * @param id
   * @return
   */
  Player get(int id);

  /**
   * Delete a player. Restricted to admins only.
   *
   * @param id
   */
  void delete(int id);

  /**
   * An email will be sent with a code
   *
   * @param email the email of the player that forget the password
   */
  void forgotPassword(String email);

  /**
   * The code sent from the forgotPassword method and a new password
   *
   * @param code
   * @param password
   */
  void resetPassword(String code, String password);
}
