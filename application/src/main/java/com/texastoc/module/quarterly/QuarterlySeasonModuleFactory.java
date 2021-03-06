package com.texastoc.module.quarterly;

import org.springframework.stereotype.Component;

@Component
public class QuarterlySeasonModuleFactory {

  private static QuarterlySeasonModule QUARTERLY_SEASON_MODULE;

  public QuarterlySeasonModuleFactory(QuarterlySeasonModuleImpl quarterlySeasonModule) {
    QUARTERLY_SEASON_MODULE = quarterlySeasonModule;
  }

  /**
   * Return a concrete class that implements the QuarterlySeasonModule interface
   *
   * @return a QuarterlySeasonModule instance
   * @throws IllegalStateException if the QuarterlySeasonModule instance is not ready
   */
  public static QuarterlySeasonModule getQuarterlySeasonModule() throws IllegalStateException {
    if (QUARTERLY_SEASON_MODULE == null) {
      throw new IllegalStateException("Quarterly season module instance not ready");
    }
    return QUARTERLY_SEASON_MODULE;
  }
}
