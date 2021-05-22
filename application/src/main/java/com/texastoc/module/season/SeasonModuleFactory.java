package com.texastoc.module.season;

import org.springframework.stereotype.Component;

@Component
public class SeasonModuleFactory {

  private static SeasonModule SEASON_MODULE;

  public SeasonModuleFactory(SeasonModuleImpl seasonModuleImpl) {
    SEASON_MODULE = seasonModuleImpl;
  }

  /**
   * Return a concrete class that implements the SeasonModule interface
   *
   * @return a SeasonModule instance
   * @throws IllegalStateException if the SeasonModule instance is not ready
   */
  public static SeasonModule getSeasonModule() throws IllegalStateException {
    if (SEASON_MODULE == null) {
      throw new IllegalStateException("Season module instance not ready");
    }
    return SEASON_MODULE;
  }
}
