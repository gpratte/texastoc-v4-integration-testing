package com.texastoc.module.settings.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.common.PointsGenerator;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import com.texastoc.module.settings.model.Version;
import com.texastoc.module.settings.repository.SettingsRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SettingsServiceTest implements TestConstants {

  private SettingsService settingsService;
  private SettingsRepository settingsRepository;
  private PointsGenerator pointsGenerator;

  @Before
  public void before() {
    settingsRepository = mock(SettingsRepository.class);
    pointsGenerator = new PointsGenerator(CHOP_TENTH_PLACE_INCR,
        CHOP_TENTH_PLACE_POINTS,
        CHOP_MULTIPLIER);
    settingsService = new SettingsService(settingsRepository, "payouts-percentages.json",
        pointsGenerator);
  }

  @Test
  public void systemSettings() {
    // Arrange
    Settings settings = new Settings();

    settings.setVersion(Version.builder()
        .version("1.1")
        .build());

    Map<Integer, TocConfig> tocConfigMap = new HashMap<>();
    tocConfigMap.put(2020, TocConfig.builder()
        .id(123)
        .build());
    settings.setTocConfigs(tocConfigMap);

    when(settingsRepository.findById(1)).thenReturn(java.util.Optional.of(settings));

    // Act
    SystemSettings actual = settingsService.get();

    // Assert
    assertEquals(123, actual.getTocConfigs().get(2020).getId());
    assertEquals("1.1", actual.getVersion().getVersion());

    Payout secondPlaceOfTwo = actual.getPayouts().get(2).get(1);
    assertEquals(2, secondPlaceOfTwo.getPlace());
    assertEquals(0.35, secondPlaceOfTwo.getPercent(), 0.0);

    Payout thirdPlaceOfThree = actual.getPayouts().get(3).get(2);
    assertEquals(3, thirdPlaceOfThree.getPlace());
    assertEquals(0.2, thirdPlaceOfThree.getPercent(), 0.0);

    Map<Integer, Integer> pointsForTwo = actual.getPoints().get(2);
    // first place points
    assertEquals(30, pointsForTwo.get(1).intValue());
    // second place points
    assertEquals(23, pointsForTwo.get(2).intValue());
    // third place points
    assertEquals(0, pointsForTwo.get(3).intValue());
  }

  @Test
  public void testJsonFileNotFound() {
    // Arrange
    Settings settings = new Settings();
    when(settingsRepository.findById(1)).thenReturn(java.util.Optional.of(settings));

    // Act
    SettingsService ss = new SettingsService(settingsRepository, "does-not-exist.json",
        pointsGenerator);
    SystemSettings actual = ss.get();

    // Assert
    assertEquals(0, actual.getPayouts().size());
  }

  // TODO travis ci cannot find the json file
  @Ignore
  @Test
  public void testBadJson() {
    // Arrange
    Settings settings = new Settings();
    when(settingsRepository.findById(1)).thenReturn(java.util.Optional.of(settings));

    // Act
    SettingsService ss = new SettingsService(settingsRepository, "bad-payout-percentages.json",
        pointsGenerator);
    SystemSettings actual = ss.get();

    // Assert
    assertEquals(0, actual.getPayouts().size());
  }

}
