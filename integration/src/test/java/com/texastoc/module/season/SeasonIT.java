package com.texastoc.module.season;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.texastoc.module.season.model.Season;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SeasonIT extends BaseSeasonIT {

  @Before
  public void before() {
    super.before();
  }

  @Test
  public void createASeason() {
    // Arrange
    seasonStarts();
    // Act
    theSeasonIsCreated();
    // Assert
    verifyStartDate();
    verifySeasonCosts();
  }

  @Test
  public void createAndRetriveASeason() {
    // create and retrieve a season
    seasonExists();
    getSeason();
    verifyStartDate();
    verifySeasonCosts();
  }

  @Test
  public void createAndEndASeason() {
    seasonExists();
    endSeason();
    getSeason();
    verifyRetrievedIsEnded();
  }

  @Test
  public void createAndEndAndOpenASeason() {
    seasonExists();
    endSeason();
    openSeason();
    getSeason();
    verifyRetrievedIsNotEnded();
  }

  // TODO add test with season players and make sure they become historical season players

  // A season starts encompassing today
  private void seasonStarts() {
    startYear = getSeasonStart().getYear();
  }

  // A season encompassing today exists
  private void seasonExists() {
    aSeasonExists();
  }

  // The season is created
  private void theSeasonIsCreated() {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(startYear, token);
  }

  // The season is ended
  private void endSeason() {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    super.endSeason(seasonCreated.getId(), token);
  }

  // The season is opened
  private void openSeason() {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    super.openSeason(seasonCreated.getId(), token);
  }

  // The season is retrieved
  private void getSeason() {
    String token = login(USER_EMAIL, USER_PASSWORD);
    seasonRetrieved = super.getSeason(seasonCreated.getId(), token);
  }

  // The start date should be May first
  private void verifyStartDate() {
    assertEquals(getSeasonStart(), seasonCreated.getStart());
  }

  // The season costs should be set
  private void verifySeasonCosts() {
    verifySeasonCosts(seasonCreated);
  }

  // The retrieved season should be ended
  private void verifyRetrievedIsEnded() {
    assertTrue(seasonRetrieved.isFinalized());
  }

  // The retrieved season should not be ended
  private void verifyRetrievedIsNotEnded() {
    assertFalse(seasonRetrieved.isFinalized());
  }

  private void verifySeasonCosts(Season season) {
    Assert.assertEquals(KITTY_PER_GAME, season.getKittyPerGameCost());
    Assert.assertEquals(TOC_PER_GAME, season.getTocPerGameCost());
    Assert.assertEquals(QUARTERLY_TOC_PER_GAME, season.getQuarterlyTocPerGameCost());
    Assert.assertEquals(QUARTERLY_NUM_PAYOUTS, season.getQuarterlyNumPayouts());
    Assert.assertEquals(GAME_BUY_IN, season.getBuyInCost());
    Assert.assertEquals(GAME_REBUY, season.getRebuyAddOnCost());
    Assert.assertEquals(GAME_REBUY_TOC_DEBIT, season.getRebuyAddOnTocDebitCost());

    assertTrue(season.getNumGames() == 52 || season.getNumGames() == 53);
    assertFalse(season.isFinalized());
  }
}