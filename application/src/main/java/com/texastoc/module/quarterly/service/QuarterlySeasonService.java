package com.texastoc.module.quarterly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.exception.BLException;
import com.texastoc.exception.BLType;
import com.texastoc.exception.ErrorDetails;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.calculator.QuarterlySeasonCalculator;
import com.texastoc.module.quarterly.model.Quarter;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuarterlySeasonService implements QuarterlySeasonModule {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final QuarterlySeasonRepository qSeasonRepository;
  private final QuarterlySeasonCalculator qSeasonCalculator;

  private SettingsModule settingsModule;

  @Autowired
  public QuarterlySeasonService(QuarterlySeasonRepository qSeasonRepository,
      QuarterlySeasonCalculator quarterlySeasonCalculator) {
    this.qSeasonRepository = qSeasonRepository;
    this.qSeasonCalculator = quarterlySeasonCalculator;
  }

  @Override
  public List<QuarterlySeason> create(int seasonId, int seasonStartYear) {
    Settings settings = getSettingsModule().get();
    TocConfig tocConfig = settings.getTocConfigs().get(seasonStartYear);

    List<QuarterlySeason> quarterlySeasons = new ArrayList<>(4);
    for (int i = 1; i <= 4; ++i) {
      LocalDate qStart = null;
      LocalDate qEnd = null;
      switch (i) {
        case 1:
          // Season start
          qStart = LocalDate.of(seasonStartYear, Month.MAY.getValue(), 1);
          // Last day in July
          qEnd = LocalDate.of(seasonStartYear, Month.AUGUST.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
        case 2:
          // First day in August
          qStart = LocalDate.of(seasonStartYear, Month.AUGUST.getValue(), 1);
          // Last day in October
          qEnd = LocalDate.of(seasonStartYear, Month.NOVEMBER.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
        case 3:
          // First day in November
          qStart = LocalDate.of(seasonStartYear, Month.NOVEMBER.getValue(), 1);
          // Last day in January
          qEnd = LocalDate.of(seasonStartYear + 1, Month.FEBRUARY.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
        case 4:
          // First day in February
          qStart = LocalDate.of(seasonStartYear + 1, Month.FEBRUARY.getValue(), 1);
          // End of season
          qEnd = LocalDate.of(seasonStartYear + 1, Month.MAY.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
      }

      // Count the number of Thursdays between the start and end inclusive
      int qNumThursdays = 0;
      LocalDate thursday = findNextThursday(qStart);
      while (thursday.isBefore(qEnd) || thursday.isEqual(qEnd)) {
        ++qNumThursdays;
        thursday = thursday.plusWeeks(1);
      }

      QuarterlySeason qSeason = QuarterlySeason.builder()
          .seasonId(seasonId)
          .quarter(Quarter.fromInt(i))
          .start(qStart)
          .end(qEnd)
          .numGames(qNumThursdays)
          .qTocPerGameCost(tocConfig.getQuarterlyTocCost())
          .numPayouts(tocConfig.getQuarterlyNumPayouts())
          .build();
      quarterlySeasons.add(qSeasonRepository.save(qSeason));
    }
    return quarterlySeasons;
  }

  @Override
  public List<QuarterlySeason> getBySeasonId(int seasonId) {
    return qSeasonRepository.findBySeasonId(seasonId);
  }

  @Override
  public QuarterlySeason getByDate(LocalDate date) {
    List<QuarterlySeason> qSeasons = qSeasonRepository.findByDate(date);
    if (qSeasons.size() > 0) {
      return qSeasons.get(0);
    }
    throw new BLException(BLType.NOT_FOUND, ErrorDetails.builder()
        .target("quarterlySeason")
        .message("for date " + date + " not found")
        .build());
  }

  private LocalDate findNextThursday(LocalDate day) {
    while (true) {
      if (day.getDayOfWeek() == DayOfWeek.THURSDAY) {
        return day;
      }
      day = day.plusDays(1);
    }
  }

  private SettingsModule getSettingsModule() {
    if (settingsModule == null) {
      settingsModule = SettingsModuleFactory.getSettingsModule();
    }
    return settingsModule;
  }

  @Override
  public void gameFinalized(GameFinalizedEvent event) {
    qSeasonCalculator.calculate(event.getQSeasonId());
  }
}
