package com.texastoc.module.game;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = "pretty",
    features = "src/test/resources/game/game_calculations.feature")
public class CucumberGameCalculationsTests {

}
