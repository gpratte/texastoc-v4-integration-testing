package com.texastoc.module.season.event;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.season.calculator.SeasonCalculator;

public class GameFinalizedHandler {

  private final SeasonCalculator seasonCalculator;

  public GameFinalizedHandler(SeasonCalculator seasonCalculator) {
    this.seasonCalculator = seasonCalculator;
  }

  public void handleGameFinalized(GameFinalizedEvent event) {
    seasonCalculator.calculate(event.getSeasonId());
  }
}
