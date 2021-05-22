package com.texastoc.module.quarterly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.texastoc.module.quarterly.model.QuarterlySeason;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class QuarterlySeasonStepdefs extends BaseQuarterlySeasonStepdefs {

  @Before
  public void before() {
    super.before();
  }

  @Given("^the season start year encompassing today$")
  public void seasonStarts() throws Exception {
    startYear = getSeasonStart().getYear();
  }

  @When("^the current season is created$")
  public void createTheSeason() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(startYear, token);
  }

  @Then("^four quarterly seasons should be created$")
  public void verifyQuarters() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    List<QuarterlySeason> quarters = getQuarterlySeasons(seasonCreated.getId(), token);
    assertEquals(4, quarters.size());
    quarters.forEach(qs -> {
      assertEquals(seasonCreated.getId(), qs.getSeasonId());
      int startYear = seasonCreated.getStart().getYear();
      switch (qs.getQuarter().getValue()) {
        case 1:
          // First day in May
          assertEquals(LocalDate.of(startYear, Month.MAY.getValue(), 1), qs.getStart());
          assertEquals(LocalDate.of(startYear, Month.AUGUST.getValue(), 1).minusDays(1),
              qs.getEnd());
          break;
        case 2:
          // First day in August
          assertEquals(LocalDate.of(startYear, Month.AUGUST.getValue(), 1), qs.getStart());
          assertEquals(LocalDate.of(startYear, Month.NOVEMBER.getValue(), 1).minusDays(1),
              qs.getEnd());
          break;
        case 3:
          // First day in November
          assertEquals(LocalDate.of(startYear, Month.NOVEMBER.getValue(), 1), qs.getStart());
          assertEquals(LocalDate.of(startYear + 1, Month.FEBRUARY.getValue(), 1).minusDays(1),
              qs.getEnd());
          break;
        case 4:
          // First day in February
          assertEquals(LocalDate.of(startYear + 1, Month.FEBRUARY.getValue(), 1), qs.getStart());
          assertEquals(LocalDate.of(startYear + 1, Month.MAY.getValue(), 1).minusDays(1),
              qs.getEnd());
          break;
        default:
          fail("Unknown quarter");
      }

      verifyQuarterlySeasonCosts(qs);
    });
  }

  private void verifyQuarterlySeasonCosts(QuarterlySeason qSeason) {
    assertEquals(QUARTERLY_TOC_PER_GAME, qSeason.getQTocPerGameCost());
    assertEquals(QUARTERLY_NUM_PAYOUTS, qSeason.getNumPayouts());
    assertEquals(0, qSeason.getQTocCollected());
    assertEquals(13, qSeason.getNumGames());
    assertEquals(0, qSeason.getNumGamesPlayed());
    assertFalse(qSeason.isFinalized());
  }

}
