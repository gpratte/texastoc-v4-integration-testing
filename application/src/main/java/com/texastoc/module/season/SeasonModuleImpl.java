package com.texastoc.module.season;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.season.calculator.SeasonCalculator;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.service.HistoricalSeasonService;
import com.texastoc.module.season.service.SeasonService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeasonModuleImpl implements SeasonModule {

  private final SeasonService seasonService;
  private final HistoricalSeasonService historicalSeasonService;
  private final SeasonCalculator seasonCalculator;

  public SeasonModuleImpl(SeasonService seasonService,
      HistoricalSeasonService historicalSeasonService, SeasonCalculator seasonCalculator) {
    this.seasonService = seasonService;
    this.historicalSeasonService = historicalSeasonService;
    this.seasonCalculator = seasonCalculator;
  }

  @Override
  public Season create(int startYear) {
    return seasonService.create(startYear);
  }

  @Override
  public Season get(int id) {
    return seasonService.get(id);
  }

  @Override
  public List<Season> getAll() {
    return seasonService.getAll();
  }

  @Override
  public Season end(int seasonId) {
    return seasonService.end(seasonId);
  }

  @Override
  public Season open(int seasonId) {
    return seasonService.open(seasonId);
  }

  @Override
  public List<HistoricalSeason> getPastSeasons() {
    return historicalSeasonService.getPastSeasons();
  }

  @Override
  public void gameFinalized(GameFinalizedEvent event) {
    seasonCalculator.calculate(event.getSeasonId());
  }
}
