package com.texastoc.module.quarterly;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.service.QuarterlySeasonService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuarterlySeasonModuleImpl implements QuarterlySeasonModule {

  private final QuarterlySeasonService quarterlySeasonService;

  public QuarterlySeasonModuleImpl(
      QuarterlySeasonService quarterlySeasonService) {
    this.quarterlySeasonService = quarterlySeasonService;
  }

  @Override
  public List<QuarterlySeason> create(int seasonId, int startYear) {
    return quarterlySeasonService.create(seasonId, startYear);
  }

  @Override
  public List<QuarterlySeason> getBySeasonId(int seasonId) {
    return quarterlySeasonService.getBySeasonId(seasonId);
  }

  @Override
  public QuarterlySeason getByDate(LocalDate date) {
    return quarterlySeasonService.getByDate(date);
  }

  @Override
  public void gameFinalized(GameFinalizedEvent gameFinalizedEvent) {
    quarterlySeasonService.gameFinalized(gameFinalizedEvent);
  }
}
