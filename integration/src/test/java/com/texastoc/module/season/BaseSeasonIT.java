package com.texastoc.module.season;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.season.model.Season;

public abstract class BaseSeasonIT extends BaseIntegrationTest {

  protected Integer startYear;
  protected Season seasonCreated;
  protected Season seasonRetrieved;
  protected Game gameCreated;

  public void before() {
    startYear = null;
    seasonCreated = null;
    seasonRetrieved = null;
    gameCreated = null;
  }

  protected void aSeasonExists() {
    // Arrange
    startYear = getSeasonStart().getYear();
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(startYear, token);
  }

}

