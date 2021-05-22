package com.texastoc.module.quarterly.calculator;

import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.model.QuarterlySeasonPayout;
import com.texastoc.module.quarterly.model.QuarterlySeasonPlayer;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuarterlySeasonCalculator {

  private final QuarterlySeasonRepository qSeasonRepository;

  private GameModule gameModule;

  public QuarterlySeasonCalculator(QuarterlySeasonRepository qSeasonRepository) {
    this.qSeasonRepository = qSeasonRepository;
  }

  public void calculate(int id) {
    QuarterlySeason qSeason = qSeasonRepository.findById(id).get();

    // Calculate quarterly season
    List<Game> games = getGameModule().getByQuarterlySeasonId(id);

    qSeason.setNumGamesPlayed(games.size());

    List<GamePlayer> gameQuarterlyTocPlayers = new LinkedList<>();
    int qTocCollected = 0;
    for (Game game : games) {
      qTocCollected += game.getQuarterlyTocCollected();
      gameQuarterlyTocPlayers.addAll(game.getPlayers().stream()
          .filter(GamePlayer::isQuarterlyTocParticipant)
          .collect(Collectors.toList())
      );
    }
    qSeason.setQTocCollected(qTocCollected);
    qSeason.setLastCalculated(LocalDateTime.now());

    // Calculate quarterly season players
    List<QuarterlySeasonPlayer> players = calculatePlayers(qSeason.getSeasonId(), id,
        gameQuarterlyTocPlayers);
    qSeason.setPlayers(players);

    // Calculate quarterly season payouts
    List<QuarterlySeasonPayout> payouts = calculatePayouts(qTocCollected, qSeason.getSeasonId(),
        id);
    qSeason.setPayouts(payouts);

    // Persist quarterly season
    qSeasonRepository.save(qSeason);
  }

  private List<QuarterlySeasonPlayer> calculatePlayers(int seasonId, int qSeasonId,
      List<GamePlayer> gamePlayers) {
    Map<Integer, QuarterlySeasonPlayer> playerMap = new HashMap<>();

    for (GamePlayer gamePlayer : gamePlayers) {
      QuarterlySeasonPlayer qSeasonPlayer = playerMap.get(gamePlayer.getPlayerId());
      if (qSeasonPlayer == null) {
        qSeasonPlayer = QuarterlySeasonPlayer.builder()
            .playerId(gamePlayer.getPlayerId())
            .seasonId(seasonId)
            .qSeasonId(qSeasonId)
            .name(gamePlayer.getName())
            .build();
        playerMap.put(gamePlayer.getPlayerId(), qSeasonPlayer);
      }

      if (gamePlayer.getQTocChopPoints() != null && gamePlayer.getQTocChopPoints() > 0) {
        qSeasonPlayer.setPoints(qSeasonPlayer.getPoints() + gamePlayer.getQTocChopPoints());
      } else if (gamePlayer.getQTocPoints() != null && gamePlayer.getQTocPoints() > 0) {
        qSeasonPlayer.setPoints(qSeasonPlayer.getPoints() + gamePlayer.getQTocPoints());
      }

      qSeasonPlayer.setEntries(qSeasonPlayer.getEntries() + 1);
    }

    List<QuarterlySeasonPlayer> qSeasonPlayers = new ArrayList<>(playerMap.values());
    Collections.sort(qSeasonPlayers);

    int place = 0;
    int lastPoints = -1;
    int numTied = 0;
    for (QuarterlySeasonPlayer player : qSeasonPlayers) {
      if (player.getPoints() > 0) {
        // check for a tie
        if (player.getPoints() == lastPoints) {
          // tie for points so same player
          player.setPlace(place);
          ++numTied;
        } else {
          place = ++place + numTied;
          player.setPlace(place);
          lastPoints = player.getPoints();
          numTied = 0;
        }
      }
    }

    return qSeasonPlayers;
  }

  private List<QuarterlySeasonPayout> calculatePayouts(int pot, int seasonId, int qSeasonId) {
    List<QuarterlySeasonPayout> payouts = new ArrayList<>(3);

    if (pot < 1) {
      return payouts;
    }

    int firstPlace = (int) Math.round(pot * 0.5d);
    int secondPlace = (int) Math.round(pot * 0.3d);
    int thirdPlace = pot - firstPlace - secondPlace;

    payouts.add(QuarterlySeasonPayout.builder()
        .seasonId(seasonId)
        .qSeasonId(qSeasonId)
        .place(1)
        .amount(firstPlace)
        .build());
    payouts.add(QuarterlySeasonPayout.builder()
        .seasonId(seasonId)
        .qSeasonId(qSeasonId)
        .place(2)
        .amount(secondPlace)
        .build());
    payouts.add(QuarterlySeasonPayout.builder()
        .seasonId(seasonId)
        .qSeasonId(qSeasonId)
        .place(3)
        .amount(thirdPlace)
        .build());

    return payouts;
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }
}