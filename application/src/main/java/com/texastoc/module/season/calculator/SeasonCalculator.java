package com.texastoc.module.season.calculator;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.model.SeasonEstimatedPayout;
import com.texastoc.module.season.model.SeasonPayout;
import com.texastoc.module.season.model.SeasonPayoutPlace;
import com.texastoc.module.season.model.SeasonPayoutRange;
import com.texastoc.module.season.model.SeasonPayoutSettings;
import com.texastoc.module.season.model.SeasonPlayer;
import com.texastoc.module.season.repository.SeasonPayoutSettingsRepository;
import com.texastoc.module.season.repository.SeasonRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SeasonCalculator {

  private final SeasonRepository seasonRepository;
  private final SeasonPayoutSettingsRepository seasonPayoutSettingsRepository;

  private GameModule gameModule;

  public SeasonCalculator(SeasonRepository seasonRepository,
      SeasonPayoutSettingsRepository seasonPayoutSettingsRepository) {
    this.seasonRepository = seasonRepository;
    this.seasonPayoutSettingsRepository = seasonPayoutSettingsRepository;
  }

  public void calculate(int id) {
    Season season = seasonRepository.findById(id).get();

    // Calculate season
    List<Game> games = getGameModule().getBySeasonId(id);

    season.setNumGamesPlayed(games.size());

    int buyInCollected = 0;
    int rebuyAddOnCollected = 0;
    int annualTocCollected = 0;
    int totalCollected = 0;

    int annualTocFromRebuyAddOnCalculated = 0;
    int rebuyAddOnLessAnnualTocCalculated = 0;
    int totalCombinedAnnualTocCalculated = 0;
    int kittyCalculated = 0;
    int prizePotCalculated = 0;

    List<GamePlayer> gameAnnualTocPlayers = new LinkedList<>();
    for (Game game : games) {
      buyInCollected += game.getBuyInCollected();
      rebuyAddOnCollected += game.getRebuyAddOnCollected();
      annualTocCollected += game.getAnnualTocCollected();
      totalCollected += game.getTotalCollected();

      annualTocFromRebuyAddOnCalculated += game.getAnnualTocFromRebuyAddOnCalculated();
      rebuyAddOnLessAnnualTocCalculated += game.getRebuyAddOnLessAnnualTocCalculated();
      totalCombinedAnnualTocCalculated +=
          game.getAnnualTocCollected() + game.getAnnualTocFromRebuyAddOnCalculated();
      kittyCalculated += game.getKittyCalculated();
      prizePotCalculated += game.getPrizePotCalculated();

      gameAnnualTocPlayers.addAll(game.getPlayers().stream()
          .filter(GamePlayer::isAnnualTocParticipant)
          .collect(Collectors.toList())
      );
    }

    season.setBuyInCollected(buyInCollected);
    season.setRebuyAddOnCollected(rebuyAddOnCollected);
    season.setAnnualTocCollected(annualTocCollected);
    season.setTotalCollected(totalCollected);

    season.setAnnualTocFromRebuyAddOnCalculated(annualTocFromRebuyAddOnCalculated);
    season.setRebuyAddOnLessAnnualTocCalculated(rebuyAddOnLessAnnualTocCalculated);
    season.setTotalCombinedAnnualTocCalculated(totalCombinedAnnualTocCalculated);
    season.setKittyCalculated(kittyCalculated);
    season.setPrizePotCalculated(prizePotCalculated);

    season.setLastCalculated(LocalDateTime.now());

    // Calculate season players
    List<SeasonPlayer> players = calculatePlayers(id, gameAnnualTocPlayers);
    season.setPlayers(players);

    // Calculate current payouts and estimated payouts
    List<SeasonPayoutSettings> seasonPayoutSettingss = seasonPayoutSettingsRepository
        .findByStartYear(season.getStart().getYear());
    if (seasonPayoutSettingss.size() < 1) {
      throw new NotFoundException(
          "Could not find the SeasonPayoutSettings for season " + season.getId());
    }
    SeasonPayoutSettings seasonPayoutSettings = seasonPayoutSettingss.get(0);
    int total = season.getTotalCombinedAnnualTocCalculated();
    if (season.getNumGamesPlayed() == season.getNumGames()) {
      season.setPayouts(calculatePayouts(total, season.getId(), false, seasonPayoutSettings));
      season.setEstimatedPayouts(Collections.emptyList());
    } else {
      season.setPayouts(Collections.emptyList());
      double seasonTocAmountPerGame = total / (double) season.getNumGamesPlayed();
      int estimatedTotal = (int) (seasonTocAmountPerGame * season.getNumGames());
      season.setEstimatedPayouts(
          calculateEstimatedPayouts(estimatedTotal, season.getId(), true, seasonPayoutSettings));
    }

    // Persist season
    seasonRepository.save(season);
  }

  private List<SeasonPlayer> calculatePlayers(int id, List<GamePlayer> gamePlayers) {
    Map<Integer, SeasonPlayer> seasonPlayerMap = new HashMap<>();

    for (GamePlayer gamePlayer : gamePlayers) {
      SeasonPlayer seasonPlayer = seasonPlayerMap.get(gamePlayer.getPlayerId());
      if (seasonPlayer == null) {
        seasonPlayer = SeasonPlayer.builder()
            .playerId(gamePlayer.getPlayerId())
            .seasonId(id)
            .name(gamePlayer.getName())
            .build();
        seasonPlayerMap.put(gamePlayer.getPlayerId(), seasonPlayer);
      }

      if (gamePlayer.getTocChopPoints() != null && gamePlayer.getTocChopPoints() > 0) {
        seasonPlayer.setPoints(seasonPlayer.getPoints() + gamePlayer.getTocChopPoints());
      } else if (gamePlayer.getTocPoints() != null && gamePlayer.getTocPoints() > 0) {
        seasonPlayer.setPoints(seasonPlayer.getPoints() + gamePlayer.getTocPoints());
      }

      seasonPlayer.setEntries(seasonPlayer.getEntries() + 1);
    }

    List<SeasonPlayer> seasonPlayers = new ArrayList<>(seasonPlayerMap.values());
    Collections.sort(seasonPlayers);

    int place = 0;
    int lastPoints = -1;
    int numTied = 0;
    for (SeasonPlayer player : seasonPlayers) {
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

    return seasonPlayers;
  }

  private List<SeasonPayout> calculatePayouts(int seasonTocAmount, int seasonId, boolean estimated,
      SeasonPayoutSettings seasonPayoutSettings) {
    List<SeasonPayout> seasonPayouts = new LinkedList<>();

    List<SeasonPayoutRange> ranges = seasonPayoutSettings.getRanges();
    if (ranges == null || ranges.size() < 1) {
      return seasonPayouts;
    }

    for (SeasonPayoutRange range : ranges) {
      if (seasonTocAmount >= range.getLowRange() && seasonTocAmount < range.getHighRange()) {
        int amountToDivy = seasonTocAmount - range.getLowRange();
        for (SeasonPayoutPlace place : range.getGuaranteed()) {
          SeasonPayout seasonPayout = calculatePayout(place, amountToDivy);
          seasonPayout.setSeasonId(seasonId);
          seasonPayout.setGuaranteed(true);
          seasonPayout.setEstimated(estimated);
          seasonPayouts.add(seasonPayout);
        }
        for (SeasonPayoutPlace place : range.getFinalTable()) {
          SeasonPayout seasonPayout = calculatePayout(place, amountToDivy);
          seasonPayout.setSeasonId(seasonId);
          seasonPayout.setGuaranteed(false);
          seasonPayout.setEstimated(estimated);
          seasonPayouts.add(seasonPayout);
        }

        int amountOfPayouts = 0;
        for (SeasonPayout seasonPayout : seasonPayouts) {
          amountOfPayouts += seasonPayout.getAmount();
        }

        int amountYetToDivy = seasonTocAmount - amountOfPayouts;
        while (amountYetToDivy > 0) {
          for (SeasonPayout seasonPayout : seasonPayouts) {
            seasonPayout.setAmount(seasonPayout.getAmount() + 1);
            if (--amountYetToDivy == 0) {
              break;
            }
          }
        }
        break;
      }
    }

    return seasonPayouts;
  }

  private List<SeasonEstimatedPayout> calculateEstimatedPayouts(int seasonTocAmount, int seasonId,
      boolean estimated, SeasonPayoutSettings seasonPayoutSettings) {
    List<SeasonPayout> seasonPayouts = calculatePayouts(seasonTocAmount, seasonId, estimated,
        seasonPayoutSettings);

    if (seasonPayouts.size() == 0) {
      return Collections.emptyList();
    }

    List<SeasonEstimatedPayout> estimatedPayouts = new ArrayList<>(seasonPayouts.size());
    seasonPayouts.forEach(sp ->
        estimatedPayouts.add(SeasonEstimatedPayout.builder()
            .seasonId(sp.getSeasonId())
            .place(sp.getPlace())
            .amount(sp.getAmount())
            .guaranteed(sp.isGuaranteed())
            .estimated(sp.isEstimated())
            .cash(sp.isCash())
            .build()));
    return estimatedPayouts;
  }

  private SeasonPayout calculatePayout(SeasonPayoutPlace place, int amountToDivy) {
    SeasonPayout seasonPayout = new SeasonPayout();
    seasonPayout.setPlace(place.getPlace());
    seasonPayout.setAmount(place.getAmount());
    if (place.getAmount() == 0) {
      seasonPayout.setCash(true);
    }
    if (amountToDivy > 0) {
      double payoutExtra = amountToDivy * (place.getPercent() / 100.0d);
      payoutExtra = Math.floor(payoutExtra);
      seasonPayout.setAmount(seasonPayout.getAmount() + (int) (payoutExtra));
    }
    return seasonPayout;
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }

}
