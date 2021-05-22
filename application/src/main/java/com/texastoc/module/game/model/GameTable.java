package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
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
public class GameTable {

  @Id
  private int id;
  private int tableNum;
  @MappedCollection
  private List<Seat> seats;

  public void addSeat(Seat seat) {
    if (seats == null) {
      seats = new ArrayList<>();
    }
    seats.add(seat);
  }
}
