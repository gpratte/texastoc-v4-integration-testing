package com.texastoc.module.game;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.season.model.Season;
import java.time.LocalDate;
import org.springframework.web.client.HttpClientErrorException;

public abstract class BaseGameStepdefs extends BaseIntegrationTest {

  protected Game gameToCreate;
  protected Game gameCreated;
  protected Game gameRetrieved;
  protected Season seasonCreated;
  protected HttpClientErrorException exception;

  protected void before() {
    // Before each scenario
    gameToCreate = null;
    gameCreated = null;
    gameRetrieved = null;
    seasonCreated = null;
    exception = null;
  }

  protected void aSeasonExists() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(token);
  }

  protected void theGameStartsNow() throws Exception {
    gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();
  }

  protected void theGameIsCreated() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);
  }

  protected void getCurrentGame() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameRetrieved = getCurrentGame(token);
  }

}
