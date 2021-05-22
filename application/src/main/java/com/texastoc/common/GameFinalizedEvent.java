package com.texastoc.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GameFinalizedEvent {

  private int gameId;
  private int seasonId;
  private int qSeasonId;
  protected boolean finalized;
}
