package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Seating {

  @Id
  private int id;
  private int gameId;
  @MappedCollection
  private List<SeatsPerTable> seatsPerTables;
  @MappedCollection
  private List<TableRequest> tableRequests;
  @MappedCollection
  private List<GameTable> gameTables;
}
