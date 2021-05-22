package com.texastoc.module.settings.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TocConfig {

  @Id
  private int id;
  private int kittyDebit;
  private int annualTocCost;
  private int quarterlyTocCost;
  private int quarterlyNumPayouts;
  private int regularBuyInCost;
  private int regularRebuyCost;
  private int regularRebuyTocDebit;
}
