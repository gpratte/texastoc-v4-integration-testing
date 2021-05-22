package com.texastoc.module.season.model;

import java.util.List;
import lombok.Data;

@Data
public class SeasonPayoutRange {

  private int lowRange;
  private int highRange;
  private List<SeasonPayoutPlace> guaranteed;
  private List<SeasonPayoutPlace> finalTable;
}
