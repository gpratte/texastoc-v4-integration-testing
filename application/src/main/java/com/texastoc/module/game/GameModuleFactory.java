package com.texastoc.module.game;

import org.springframework.stereotype.Component;

@Component
public class GameModuleFactory {

  private static GameModule GAME_MODULE;

  public GameModuleFactory(GameModuleImpl gameModule) {
    GAME_MODULE = gameModule;
  }

  /**
   * Return a concrete class that implements the GameModule interface
   *
   * @return a GameModule instance
   * @throws IllegalStateException if the GameModule instance is not ready
   */
  public static GameModule getGameModule() throws IllegalStateException {
    if (GAME_MODULE == null) {
      throw new IllegalStateException("Game module instance not ready");
    }
    return GAME_MODULE;
  }
}
