package com.texastoc.module.quarterly.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlySeason {

  // Read-only id set when created
  @Id
  private int id;

  // Read/write fields from API
  private int seasonId;
  private LocalDate start;
  private LocalDate end;

  // Read-only fields set by the server
  private Quarter quarter;
  private int numPayouts;

  // Setup variables. End with "Cost"
  // Read-only fields set by the server
  private int qTocPerGameCost;

  // End with "Collected" for physical money collected
  // Read-only fields set by the server
  private int qTocCollected;

  // End with "Calculated" for the where the money goes.
  // Read-only fields set by the server
  private LocalDateTime lastCalculated;

  // Other runtime variables
  // Read-only fields set by the server
  private int numGames;
  private int numGamesPlayed;
  private boolean finalized;

  @MappedCollection
  private List<QuarterlySeasonPlayer> players;
  @MappedCollection
  private List<QuarterlySeasonPayout> payouts;
}
