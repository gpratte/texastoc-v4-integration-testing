package com.texastoc.module.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.texastoc.module.player.exception.CannotDeletePlayerException;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.service.PlayerService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class PlayerRestController implements PlayerModule {

  private final PlayerService playerService;

  public PlayerRestController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @Override
  @PostMapping("/players")
  public Player create(@RequestBody Player player) {
    return playerService.create(player);
  }

  @PutMapping("/players/{id}")
  public Player update(@PathVariable("id") int id, @RequestBody @Valid Player player,
      HttpServletRequest request) {
    player.setId(id);
    return update(player);
  }

  @Override
  public Player update(Player player) {
    return playerService.update(player);
  }

  @Override
  @GetMapping("/players")
  public List<Player> getAll() {
    return playerService.getAll();
  }

  @Override
  @GetMapping("/players/{id}")
  public Player get(@PathVariable("id") int id) {
    return playerService.get(id);
  }

  @Override
  @DeleteMapping("/players/{id}")
  public void delete(@PathVariable("id") int id) {
    playerService.delete(id);
  }

  @PostMapping(value = "/password/reset", consumes = "application/vnd.texastoc.password-forgot+json")
  public void forgot(@RequestBody Forgot forgot) {
    forgotPassword(forgot.getEmail());
  }

  @Override
  public void forgotPassword(String email) {
    playerService.forgotPassword(email);
  }

  @PostMapping(value = "/password/reset", consumes = "application/vnd.texastoc.password-reset+json")
  public void reset(@RequestBody Reset reset) {
    resetPassword(reset.getCode(), reset.getPassword());
  }

  @Override
  public void resetPassword(String code, String password) {
    playerService.resetPassword(code, password);
  }

  @Override
  @PostMapping("/players/{id}/roles")
  public Player addRole(@PathVariable("id") int id, @RequestBody @Valid Role role) {
    return playerService.addRole(id, role);
  }

  @Override
  @DeleteMapping("/players/{id}/roles/{roleId}")
  public Player removeRole(@PathVariable("id") int id, @PathVariable("roleId") int roleId) {
    return playerService.removeRole(id, roleId);
  }

  @ExceptionHandler(value = {CannotDeletePlayerException.class})
  protected void handleCannotDeletePlayerException(CannotDeletePlayerException ex,
      HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
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
