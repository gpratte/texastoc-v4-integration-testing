package com.texastoc.module.quarterly;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import java.time.LocalDate;
import java.util.List;

public interface QuarterlySeasonModule {

  /**
   * Create the four quarterly seasons
   *
   * @param seasonId  the season Id
   * @param startYear the year the season starts
   */
  List<QuarterlySeason> create(int seasonId, int startYear);

  List<QuarterlySeason> getBySeasonId(int seasonId);

  QuarterlySeason getByDate(LocalDate date);

  /**
   * Used as a alternative to event messaging. Hopefully temporary until Heroku fixed the webrunner
   * hosing with Spring Integration
   *
   * @param gameFinalizedEvent
   */
  void gameFinalized(GameFinalizedEvent gameFinalizedEvent);
}
