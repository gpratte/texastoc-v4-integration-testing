package com.texastoc.module.season.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class HistoricalSeason {

  @Id
  private String startYear;
  private String endYear;
  @MappedCollection
  private List<HistoricalSeasonPlayer> players;

  @Builder
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class HistoricalSeasonPlayer {

    @Id
    private int id;
    private String name;
    private int points;
    private int entries;
  }
}
