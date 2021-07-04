package com.texastoc.module.clock.event;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.clock.service.ClockService;

public class GameFinalizedHandler {

  private final ClockService clockService;

  public GameFinalizedHandler(ClockService clockService) {
    this.clockService = clockService;
  }

  public void handleGameFinalized(GameFinalizedEvent event) {
    clockService.endClock(event.getGameId());
  }
}
