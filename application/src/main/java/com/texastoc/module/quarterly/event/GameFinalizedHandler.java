package com.texastoc.module.quarterly.event;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.quarterly.calculator.QuarterlySeasonCalculator;

public class GameFinalizedHandler {

  private final QuarterlySeasonCalculator quarterlySeasonCalculator;

  public GameFinalizedHandler(
      QuarterlySeasonCalculator quarterlySeasonCalculator) {
    this.quarterlySeasonCalculator = quarterlySeasonCalculator;
  }

  public void handleGameFinalized(GameFinalizedEvent event) {
    quarterlySeasonCalculator.calculate(event.getQSeasonId());
  }

}
