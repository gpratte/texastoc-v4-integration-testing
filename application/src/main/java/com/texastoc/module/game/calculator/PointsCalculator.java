package com.texastoc.module.game.calculator;

import com.texastoc.common.PointsGenerator;
import com.texastoc.module.game.calculator.icm.ICMCalculator;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PointsCalculator {

  private final PointsGenerator pointsGenerator;
  private final GameRepository gameRepository;

  private final Map<Integer, Map<Integer, Integer>> POINT_SYSTEM = new HashMap<>();

  public PointsCalculator(PointsGenerator pointsGenerator, GameRepository gameRepository) {
    this.pointsGenerator = pointsGenerator;
    this.gameRepository = gameRepository;
  }

  public void calculate(Game game) {
    // Get the points for a game with given number of players
    Map<Integer, Integer> placePoints = pointsGenerator.generatePlacePoints(game.getNumPlayers());

    // Apply the chop
    Map<Integer, Integer> placeChopPoints = chopPoints(game.getPlayers(), placePoints);

    // Apply the points to players that participate in either annual or quarterly toc
    for (GamePlayer gamePlayer : game.getPlayers()) {
      // Remove points
      gamePlayer.setTocPoints(null);
      gamePlayer.setTocChopPoints(null);
      gamePlayer.setQTocPoints(null);
      gamePlayer.setQTocChopPoints(null);
      
      if (gamePlayer.getPlace() != null && gamePlayer.getPlace() < 11) {
        if (gamePlayer.isAnnualTocParticipant()) {
          gamePlayer.setTocPoints(placePoints.get(gamePlayer.getPlace()));
          if (placeChopPoints != null && placeChopPoints.get(gamePlayer.getPlace()) != null) {
            gamePlayer.setTocChopPoints(placeChopPoints.get(gamePlayer.getPlace()));
          }
        }
        if (gamePlayer.isQuarterlyTocParticipant()) {
          gamePlayer.setQTocPoints(placePoints.get(gamePlayer.getPlace()));
          if (placeChopPoints != null && placeChopPoints.get(gamePlayer.getPlace()) != null) {
            gamePlayer.setQTocChopPoints(placeChopPoints.get(gamePlayer.getPlace()));
          }
        }
      }
    }

    gameRepository.save(game);
  }

  private Map<Integer, Integer> chopPoints(List<GamePlayer> gamePlayers,
      Map<Integer, Integer> placePoints) {
    List<Integer> chips = new LinkedList<>();
    outer:
    for (int i = 1; i <= 10; i++) {
      for (GamePlayer gamePlayer : gamePlayers) {
        if (gamePlayer.getPlace() != null && gamePlayer.getPlace() == i) {
          if (gamePlayer.getChop() == null) {
            break outer;
          }
          chips.add(gamePlayer.getChop());
        }
      }
    }

    if (chips.size() == 0) {
      return null;
    }

    int sumOriginal = 0;
    List<Integer> originalPoints = new ArrayList<>(chips.size());
    for (int i = 0; i < chips.size(); i++) {
      Integer original = placePoints.get(i + 1);
      originalPoints.add(original);
      sumOriginal += original;
    }
    List<Double> chopPointsWithDecmials = ICMCalculator.calculate(originalPoints, chips);

    // Round the chopped amounts
    List<Integer> chopAmountsRounded = new ArrayList<>(chips.size());
    for (Double chopAmountsWithDecmial : chopPointsWithDecmials) {
      int chopAmountRounded = (int) Math.round(chopAmountsWithDecmial);
      chopAmountsRounded.add(chopAmountRounded);
    }

    // Make sure the sum of the rounded amounts is the same as the sum of the original amounts
    ChopUtils.adjustTotal(sumOriginal, chopAmountsRounded);

    Map<Integer, Integer> chopPoints = new HashMap<>();
    for (int i = 0; i < chips.size(); i++) {
      chopPoints.put(i + 1, chopAmountsRounded.get(i));
    }
    return chopPoints;
  }
}
