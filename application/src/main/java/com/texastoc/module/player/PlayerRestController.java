package com.texastoc.module.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class PlayerRestController {

  private final PlayerModule playerModule;

  public PlayerRestController(PlayerModuleImpl playerModule) {
    this.playerModule = playerModule;
  }

  @PostMapping("/players")
  @ResponseStatus(HttpStatus.CREATED)
  public Player create(@RequestBody Player player, HttpServletRequest request) {
    return playerModule.create(player);
  }

  @PutMapping("/players/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Player update(@PathVariable("id") int id, @RequestBody @Valid Player player,
      HttpServletRequest request) {
    player.setId(id);
    return update(player);
  }

  public Player update(Player player) {
    return playerModule.update(player);
  }

  @GetMapping("/players")
  @ResponseStatus(HttpStatus.OK)
  public List<Player> getAll(HttpServletRequest request) {
    return playerModule.getAll();
  }

  @GetMapping("/players/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Player get(@PathVariable("id") int id, HttpServletRequest request) {
    return playerModule.get(id);
  }

  @DeleteMapping("/players/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") int id, HttpServletRequest request) {
    playerModule.delete(id);
  }

  @PostMapping(value = "/password/reset", consumes = "application/vnd.texastoc.password-forgot+json")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void forgot(@RequestBody Forgot forgot, HttpServletRequest request) {
    forgotPassword(forgot.getEmail());
  }

  public void forgotPassword(String email) {
    playerModule.forgotPassword(email);
  }

  @PostMapping(value = "/password/reset", consumes = "application/vnd.texastoc.password-reset+json")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reset(@RequestBody Reset reset, HttpServletRequest request) {
    resetPassword(reset.getCode(), reset.getPassword());
  }

  public void resetPassword(String code, String password) {
    playerModule.resetPassword(code, password);
  }

  @PostMapping("/players/{id}/roles")
  @ResponseStatus(HttpStatus.OK)
  public Player addRole(@PathVariable("id") int id, @RequestBody @Valid Role role,
      HttpServletRequest request) {
    return playerModule.addRole(id, role);
  }

  @DeleteMapping("/players/{id}/roles/{roleId}")
  @ResponseStatus(HttpStatus.OK)
  public Player removeRole(@PathVariable("id") int id, @PathVariable("roleId") int roleId,
      HttpServletRequest request) {
    return playerModule.removeRole(id, roleId);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Forgot {

    private String email;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Reset {

    private String code;
    private String password;
  }
}
