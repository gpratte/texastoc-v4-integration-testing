package com.texastoc.module.player;

import org.springframework.stereotype.Component;

@Component
public class PlayerModuleFactory {

  private static PlayerModule PLAYER_MODULE;

  public PlayerModuleFactory(PlayerModuleImpl playerModule) {
    PLAYER_MODULE = playerModule;
  }

  /**
   * Return a concrete class that implements the PlayerModule interface
   *
   * @return a PlayerModule instance
   * @throws IllegalStateException if the PlayerModule instance is not ready
   */
  public static PlayerModule getPlayerModule() throws IllegalStateException {
    if (PLAYER_MODULE == null) {
      throw new IllegalStateException("Player module instance not ready");
    }
    return PLAYER_MODULE;
  }
}
