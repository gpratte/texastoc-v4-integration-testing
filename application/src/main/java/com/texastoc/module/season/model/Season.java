package com.texastoc.module.season.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Season {

  // Read-only id set when created
  @Id
  private int id;

  // Read-only fields set by the server
  private LocalDate start;
  private LocalDate end;

  // Read-only fields set by the server
  private int quarterlyNumPayouts;

  // Setup variables. End with "Cost"
  // Read-only fields set by the server
  private int kittyPerGameCost;
  private int buyInCost;
  private int tocPerGameCost;
  private int quarterlyTocPerGameCost;
  private int rebuyAddOnCost;
  private int rebuyAddOnTocDebitCost;

  // End with "Collected" for physical money collected
  // Read-only fields set by the server
  private int buyInCollected;
  // money in for rebuy add on
  private int rebuyAddOnCollected;
  // money in for annual toc
  private int annualTocCollected;
  // all physical money collected which is buy-in, rebuy add on, annual toc
  private int totalCollected;

  // End with "Calculated" for the where the money goes.
  // Read-only fields set by the server
  private int annualTocFromRebuyAddOnCalculated;
  // rebuy add on minus amount that goes to annual toc
  private int rebuyAddOnLessAnnualTocCalculated;
  // annual toc, annual toc from rebuy add on
  private int totalCombinedAnnualTocCalculated;
  // amount that goes to the kitty for supplies
  private int kittyCalculated;
  // total collected minus total combined toc collected minus kitty
  private int prizePotCalculated;

  // Other runtime variables
  // Read-only fields set by the server
  private int numGames;
  private int numGamesPlayed;
  private LocalDateTime lastCalculated;
  private boolean finalized;

  @MappedCollection
  private List<SeasonPlayer> players;
  @MappedCollection
  private List<SeasonPayout> payouts;
  @MappedCollection
  private List<SeasonEstimatedPayout> estimatedPayouts;
}
