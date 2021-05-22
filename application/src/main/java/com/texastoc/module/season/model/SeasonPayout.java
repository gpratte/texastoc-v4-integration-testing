package com.texastoc.module.season.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class SeasonPayout {

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
