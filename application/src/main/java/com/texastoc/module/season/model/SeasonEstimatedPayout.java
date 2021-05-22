package com.texastoc.module.season.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeasonEstimatedPayout {

  // Read-only id set when game player is created
  @Id
  private int id;

  // Read-only id set from server
  private int seasonId;
  private int place;
  private int amount;
  private boolean guaranteed;
  private boolean estimated;
  private boolean cash;
}
