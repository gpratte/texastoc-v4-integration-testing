package com.texastoc.module.season.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonPlayer implements Comparable<SeasonPlayer> {

  // Read-only id set when game player is created
  @Id
  private int id;

  // Read/write fields from API
  private boolean forfeit;

  // Read-only id set from server
  private int playerId;
  private int seasonId;
  private String name;
  private int entries;
  private int points;
  private Integer place;

  @Override
  public int compareTo(SeasonPlayer other) {
    // If I do not have a points and the other does then I come after
    if (points == 0) {
      if (other.points > 0) {
        return 1;
      }
    }

    // If I have points
    if (points > 0) {
      // the other does not then I come before other
      if (other.points == 0) {
        return -1;
      }
      // if I have more points I come before
      if (points > other.points) {
        return -1;
      }
      // If the points are equal then we are the same
      if (points == other.points) {
        return 0;
      }
      // if I have less points I come after
      if (points < other.points) {
        return 1;
      }
    }

    // If I don't have a name
    if (name == null) {
      // then I come after other
      return 1;
    }

    // If other doesn't have a name
    if (other.name == null) {
      // then I come before other
      return -1;
    }

    return name.toLowerCase().compareTo(other.name.toLowerCase());
  }
}
