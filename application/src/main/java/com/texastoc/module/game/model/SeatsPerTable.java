package com.texastoc.module.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatsPerTable {

  @Id
  private int id;
  private int numSeats;
  private int tableNum;
}
