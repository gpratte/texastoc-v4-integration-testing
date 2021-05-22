package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GamePayout {

  @Id
  private int id;
  private int gameId;
  private int place;
  private int amount;
  private Integer chopAmount;
  //private Double chopPercent;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GamePayout that = (GamePayout) o;
    return gameId == that.gameId &&
        place == that.place &&
        amount == that.amount &&
        Objects.equals(chopAmount, that.chopAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, place, amount, chopAmount);
  }
}
