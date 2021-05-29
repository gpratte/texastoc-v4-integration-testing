package com.texastoc.module.season;

import com.texastoc.common.GameFinalizedEvent;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.season.exception.GameInProgressException;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import java.util.List;

public interface SeasonModule {

  /**
   * Create a season. It will start May 1st of the given year.
   *
   * @param startYear the year the season should start
   * @return the newly created season
   */
  Season create(int startYear);

  /**
   * Get a season for the given season Id
   *
   * @param id the season id
   * @return the season
   * @throws NotFoundException
   */
  Season get(int id);

  /**
   * Finalizes the season for the given season Id. Creates an historical season entry.
   *
   * @param seasonId the season's id
   * @throws GameInProgressException
   * @throws NotFoundException
   */
  void end(int seasonId);

  /**
   * Unfinalizes the season for the given season Id. Removes the historical season entry if one
   * exists.
   *
   * @param seasonId the season's id
   * @throws NotFoundException
   */
  void open(int seasonId);

  /**
   * Return a list of the past seasons
   *
   * @return the past seasons
   */
  List<HistoricalSeason> getPastSeasons();

  /**
   * Used as a alternative to event messaging. Hopefully temporary until Heroku fixed the webrunner
   * hosing with Spring Integration
   *
   * @param gameFinalizedEvent
   */
  void gameFinalized(GameFinalizedEvent gameFinalizedEvent);

}
