package com.texastoc.module.game.model.clock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clock {

  private int gameId;
  private int minutes;
  private int seconds;
  private boolean playing;
  private Round thisRound;
  private Round nextRound;
  private long millisRemaining;
}
