package com.texastoc.common;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PointsGenerator {

  private final Double tenthPlaceIncr;
  private final Integer tenthPlacePoints;
  private final Double multiplier;

  private final Map<Integer, Map<Integer, Integer>> POINT_SYSTEM = new HashMap<>();

  public PointsGenerator(@Value("${points.tenthPlaceIncr}") Double tenthPlaceIncr,
      @Value("${points.tenthPlacePoints}") Integer tenthPlacePoints,
      @Value("${points.multiplier}") Double multiplier) {
    this.tenthPlaceIncr = tenthPlaceIncr;
    this.tenthPlacePoints = tenthPlacePoints;
    this.multiplier = multiplier;
  }

  /**
   * If the place/points Set is in a map for the number of players return it.
   * <p>
   * Otherwise calculate the Set of place/points for the number of players, add it to the map and
   * return it.
   */
  public Map<Integer, Integer> generatePlacePoints(int numPlayers) {
    if (POINT_SYSTEM.get(numPlayers) != null) {
      return POINT_SYSTEM.get(numPlayers);
    }

    Map<Integer, Integer> placePoints = new HashMap<>();

    double value = tenthPlacePoints;

    for (int i = 2; i < numPlayers; ++i) {
      value += tenthPlaceIncr;
    }

    int players = Math.min(numPlayers, 10);

    if (players == 10) {
      placePoints.put(10, Long.valueOf(Math.round(value)).intValue());
    } else {
      placePoints.put(10, 0);
    }

    for (int i = 9; i > 0; --i) {
      value *= multiplier;
      if (players >= i) {
        placePoints.put(i, Long.valueOf(Math.round(value))
            .intValue());
      } else {
        placePoints.put(i, 0);
      }
    }

    POINT_SYSTEM.put(numPlayers, placePoints);
    return placePoints;
  }
}
