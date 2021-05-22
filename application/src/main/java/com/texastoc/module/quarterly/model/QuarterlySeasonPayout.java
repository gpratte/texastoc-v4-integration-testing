package com.texastoc.module.quarterly.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlySeasonPayout {

  // Read-only id set when game player is created
  @Id
  private int id;

  // Read-only id set from server
  private int seasonId;
  private int qSeasonId;
  private int place;
  private int amount;
}
