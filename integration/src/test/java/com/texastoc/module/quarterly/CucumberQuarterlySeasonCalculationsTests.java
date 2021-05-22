package com.texastoc.module.quarterly;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/quarterly/quarterly_season_calculations.feature")
public class CucumberQuarterlySeasonCalculationsTests {

}
