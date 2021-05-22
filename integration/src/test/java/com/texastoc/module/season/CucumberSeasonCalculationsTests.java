package com.texastoc.module.season;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/season/season_calculations.feature")
public class CucumberSeasonCalculationsTests {

}
