package com.texastoc.module.game.service;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.game.event.GameEventing;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.QuarterlySeasonModuleFactory;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.SeasonModuleFactory;

/**
 * ;;!! TODO
 */
public class GameEventingRestImpl implements GameEventing {

  private QuarterlySeasonModule quarterlySeasonModule;
  private SeasonModule seasonModule;

  @Override
  public void send(GameFinalizedEvent event) {
    getQuarterlySeasonModule().gameFinalized(event);
    getSeasonModule().gameFinalized(event);
  }

  private QuarterlySeasonModule getQuarterlySeasonModule() {
    if (quarterlySeasonModule == null) {
      quarterlySeasonModule = QuarterlySeasonModuleFactory.getQuarterlySeasonModule();
    }
    return quarterlySeasonModule;
  }

  private SeasonModule getSeasonModule() {
    if (seasonModule == null) {
      seasonModule = SeasonModuleFactory.getSeasonModule();
    }
    return seasonModule;
  }

}
