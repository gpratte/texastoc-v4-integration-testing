package com.texastoc.module.season;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.texastoc.module.season.model.Season;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class SeasonStepdefs extends BaseSeasonStepdefs {

  @Before
  public void before() {
    super.before();
  }

  @Given("^season starts encompassing today$")
  public void seasonStarts() throws Exception {
    // Arrange
    startYear = getSeasonStart().getYear();
  }

  @Given("^a season encompassing today exists$")
  public void seasonExists() throws Exception {
    aSeasonExists();
  }

  @When("^the season is created$")
  public void the_season_is_created() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(startYear, token);
  }

  @When("^the season is ended$")
  public void endSeason() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    super.endSeason(seasonCreated.getId(), token);
  }

  @When("^the season is opened$")
  public void openSeason() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    super.openSeason(seasonCreated.getId(), token);
  }

  @And("^the season is retrieved$")
  public void getSeason() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    seasonRetrieved = super.getSeason(seasonCreated.getId(), token);
  }


  @Then("^the start date should be May first$")
  public void verifyStartDate() throws Exception {
    assertEquals(getSeasonStart(), seasonCreated.getStart());
  }

  @Then("^the retrieved season start date should be May first$")
  public void verifyRetrievedStartDate() throws Exception {
    assertEquals(getSeasonStart(), seasonRetrieved.getStart());
  }

  @And("^the current season is retrieved$")
  public void getCurrentSeason() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    seasonRetrieved = super.getCurrentSeason(token);
  }

  @And("^the season costs should be set$")
  public void verifySeasonCosts() throws Exception {
    verifySeasonCosts(seasonCreated);
  }

  @And("^the retrieved season costs should be set$")
  public void verifyRetrivedSeasonCosts() throws Exception {
    verifySeasonCosts(seasonRetrieved);
  }

  @Then("^the retrieved season should be ended$")
  public void verifyRetrievedIsEnded() throws Exception {
    assertTrue(seasonRetrieved.isFinalized());
  }

  @Then("^the retrieved season should not be ended$")
  public void verifyRetrievedIsNotEnded() throws Exception {
    assertFalse(seasonRetrieved.isFinalized());
  }

  private void verifySeasonCosts(Season season) throws Exception {
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

