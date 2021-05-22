package com.texastoc.module.game.event;

import com.texastoc.common.GameFinalizedEvent;

public interface GameEventing {

  void send(GameFinalizedEvent event);
}
