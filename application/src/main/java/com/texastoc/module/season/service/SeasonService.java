package com.texastoc.module.season.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.config.IntegrationTestingConfig;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.QuarterlySeasonModuleFactory;
import com.texastoc.module.season.exception.DuplicateSeasonException;
import com.texastoc.module.season.exception.GameInProgressException;
import com.texastoc.module.season.exception.SeasonInProgressException;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.HistoricalSeason.HistoricalSeasonPlayer;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.repository.SeasonHistoryRepository;
import com.texastoc.module.season.repository.SeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SeasonService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SeasonRepository seasonRepository;
  private final SeasonHistoryRepository seasonHistoryRepository;
  private final IntegrationTestingConfig integrationTestingConfig;

  private GameModule gameModule;
  private SettingsModule settingsModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  @Autowired
  public SeasonService(SeasonRepository seasonRepository,
      SeasonHistoryRepository seasonHistoryRepository,
      IntegrationTestingConfig integrationTestingConfig) {
    this.seasonRepository = seasonRepository;
    this.seasonHistoryRepository = seasonHistoryRepository;
    this.integrationTestingConfig = integrationTestingConfig;
  }

  @CacheEvict(value = {"seasonById", "allSeasons"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public Season create(int startYear) {
    LocalDate start = LocalDate.of(startYear, Month.MAY.getValue(), 1);

    // Make sure not overlapping with another season
    if (!integrationTestingConfig.isAllowMultipleSeasons()) {
      List<Season> seasons = getAll();
      seasons.forEach(season -> {
        if (!season.isFinalized()) {
          throw new SeasonInProgressException(startYear);
        }
      });
      seasons.forEach(season -> {
        if (season.getStart().getYear() == startYear) {
          throw new DuplicateSeasonException(startYear);
        }
      });
    }

    // The end will be the day before the start date next year
    LocalDate end = start.plusYears(1).minusDays(1);

    Settings settings = getSettingsModule().get();
    TocConfig tocConfig = settings.getTocConfigs().get(startYear);

    // Count the number of Thursdays between the start and end inclusive
    int numThursdays = 0;
    LocalDate thursday = findNextThursday(start);
    while (thursday.isBefore(end) || thursday.isEqual(end)) {
      ++numThursdays;
      thursday = thursday.plusWeeks(1);
    }

    Season newSeason = Season.builder()
        .start(start)
        .end(end)
        .kittyPerGameCost(tocConfig.getKittyDebit())
        .tocPerGameCost(tocConfig.getAnnualTocCost())
        .quarterlyTocPerGameCost(tocConfig.getQuarterlyTocCost())
        .quarterlyNumPayouts(tocConfig.getQuarterlyNumPayouts())
        .buyInCost(tocConfig.getRegularBuyInCost())
        .rebuyAddOnCost(tocConfig.getRegularRebuyCost())
        .rebuyAddOnTocDebitCost(tocConfig.getRegularRebuyTocDebit())
        .numGames(numThursdays)
        .build();

    Season season = seasonRepository.save(newSeason);

    // TODO message instead
    getQuarterlySeasonModule().create(season.getId(), season.getStart().getYear());

    return season;
  }

  @Cacheable("seasonById")
  @Transactional(readOnly = true)
  public Season get(int id) {
    Optional<Season> optionalSeason = seasonRepository.findById(id);
    if (!optionalSeason.isPresent()) {
      throw new NotFoundException("Season with id " + id + " not found");
    }
    return optionalSeason.get();

  }

  @Cacheable("allSeasons")
  public List<Season> getAll() {
    return StreamSupport.stream(seasonRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @CacheEvict(value = {"seasonById", "allSeasons"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public Season end(int seasonId) {
    Season season = get(seasonId);
    // Make sure no games are open
    List<Game> games = getGameModule().getBySeasonId(seasonId);
    for (Game game : games) {
      if (!game.isFinalized()) {
        throw new GameInProgressException("There is a game in progress");
      }
    }

    season.setFinalized(true);
    seasonRepository.save(season);

    // Clear out the historical season
    seasonHistoryRepository.deleteById(Integer.toString(season.getStart().getYear()));
    // Set the historical season
    List<HistoricalSeasonPlayer> hsPlayers = new LinkedList<>();
    HistoricalSeason historicalSeason = HistoricalSeason.builder()
        .startYear(Integer.toString(season.getStart().getYear()))
        .endYear(Integer.toString(season.getEnd().getYear()))
        .players(hsPlayers)
        .build();
    season.getPlayers()
        .forEach(seasonPlayer -> hsPlayers.add(HistoricalSeasonPlayer.builder()
            .name(seasonPlayer.getName())
            .points(seasonPlayer.getPoints())
            .entries(seasonPlayer.getEntries())
            .build()));
    seasonHistoryRepository.save(historicalSeason);

    return season;
  }

  @CacheEvict(value = {"seasonById", "allSeasons"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public Season open(int seasonId) {
    Season season = get(seasonId);

    if (!season.isFinalized()) {
      return season;
    }

    season.setFinalized(false);
    seasonRepository.save(season);

    // Clear out the historical season
    seasonHistoryRepository.deleteById(Integer.toString(season.getStart().getYear()));

    return season;
  }

  private LocalDate findNextThursday(LocalDate day) {
    while (true) {
      if (day.getDayOfWeek() == DayOfWeek.THURSDAY) {
        return day;
      }
      day = day.plusDays(1);
    }
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }

  private SettingsModule getSettingsModule() {
    if (settingsModule == null) {
      settingsModule = SettingsModuleFactory.getSettingsModule();
    }
    return settingsModule;
  }

  private QuarterlySeasonModule getQuarterlySeasonModule() {
    if (quarterlySeasonModule == null) {
      quarterlySeasonModule = QuarterlySeasonModuleFactory.getQuarterlySeasonModule();
    }
    return quarterlySeasonModule;
  }
}
