package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GamePlayer implements Comparable<GamePlayer> {

  // Read-only id set when game player is created
  @Id
  private int id;

  // Read/write fields from API
  private int playerId;
  private int gameId;
  private boolean boughtIn;
  private boolean rebought;
  private boolean annualTocParticipant;
  private boolean quarterlyTocParticipant;
  private boolean roundUpdates;
  private Integer place;
  private boolean knockedOut;
  private Integer chop;

  // Read-only fields set by the server
  private int seasonId;
  private int qSeasonId;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private Integer tocPoints;
  private Integer tocChopPoints;
  private Integer qTocPoints;
  private Integer qTocChopPoints;

  // End with "Collected" for physical money collected.
  // Read-only fields set by the server
  private Integer buyInCollected;
  private Integer rebuyAddOnCollected;
  private Integer annualTocCollected;
  private Integer quarterlyTocCollected;

  public String getName() {
    String name = null;

    if (firstName != null) {
      name = firstName;
      if (lastName != null) {
        name += " " + lastName;
      }
    } else if (lastName != null) {
      name = lastName;
    }

    return name == null ? "Unknown" : name;
  }

  @Override
  public int compareTo(GamePlayer other) {
    // If I do not have a place and the other does then I come after
    if (place == null || place.intValue() > 10) {
      if (other.place != null && other.place <= 10) {
        return 1;
      }
    }

    // If I have a place
    if (place != null && place.intValue() <= 10) {
      // the other does not then I come before other
      if (other.place == null || other.place.intValue() > 10) {
        return -1;
      }
      // the other place is smaller than mine then I come after
      if (place.intValue() > other.place.intValue()) {
        return 1;
      }
      // If the place are equal then we are the same
      if (place.intValue() == other.place.intValue()) {
        return 0;
      }
      // the other place is larger than mine then I come before
      if (place.intValue() < other.place.intValue()) {
        return -1;
      }
    }

    // If I don't have a first or a last
    if (firstName == null && lastName == null) {
      // then I come after other
      return 1;
    }

    // If other doesn't have a first or a last
    if (other.firstName == null && other.lastName == null) {
      // then I come before other
      return -1;
    }

    return makeFullName(this).compareTo(makeFullName(other));
  }

  private String makeFullName(GamePlayer player) {
    // Combine the first and last into a full name
    StringBuffer fullName = new StringBuffer();
    if (!StringUtils.isBlank(player.firstName)) {
      fullName.append(player.firstName);
    }
    if (!StringUtils.isBlank(player.firstName) && !StringUtils.isBlank(player.lastName)) {
      fullName.append(" ");
    }
    if (!StringUtils.isBlank(player.lastName)) {
      fullName.append(player.lastName);
    }
    return fullName.toString().toLowerCase();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GamePlayer that = (GamePlayer) o;
    return playerId == that.playerId &&
        gameId == that.gameId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerId, gameId);
  }
}
