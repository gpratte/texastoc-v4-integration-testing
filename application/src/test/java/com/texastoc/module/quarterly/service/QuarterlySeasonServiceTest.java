package com.texastoc.module.quarterly.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.quarterly.calculator.QuarterlySeasonCalculator;
import com.texastoc.module.quarterly.model.Quarter;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class QuarterlySeasonServiceTest implements TestConstants {

  private static final int START_YEAR = 2020;

  private QuarterlySeasonService qSeasonService;

  private QuarterlySeasonRepository qSeasonRepository;
  private SettingsModule settingsModule;

  @Before
  public void setUp() {
    qSeasonRepository = mock(QuarterlySeasonRepository.class);
    QuarterlySeasonCalculator qSeasonCalculator = mock(QuarterlySeasonCalculator.class);
    qSeasonService = new QuarterlySeasonService(qSeasonRepository, qSeasonCalculator);
    settingsModule = mock(SettingsModule.class);
    ReflectionTestUtils.setField(qSeasonService, "settingsModule", settingsModule);
  }

  @Test
  public void createQuarterlySeason() {
    // Arrange
    Map<Integer, TocConfig> tocConfigMap = new HashMap<>();
    tocConfigMap.put(START_YEAR, TestConstants.getTocConfig());
    SystemSettings systemSettings = new SystemSettings();
    systemSettings.setTocConfigs(tocConfigMap);
    when(settingsModule.get()).thenReturn(systemSettings);

    // Act
    qSeasonService.create(0, START_YEAR);

    // Assert
    ArgumentCaptor<QuarterlySeason> qSeasonArg = ArgumentCaptor.forClass(QuarterlySeason.class);
    verify(qSeasonRepository, Mockito.times(4)).save(qSeasonArg.capture());
    List<Quarter> quarters = new ArrayList<>(4);
    qSeasonArg.getAllValues().forEach(qs -> {
      assertThat(quarters).doesNotContain(qs.getQuarter());
      quarters.add(qs.getQuarter());
      assertQuarterlySeason(qs);
    });
  }

  @Test
  public void getBySeasonId() {
    // Arrange
    when(qSeasonRepository.findBySeasonId(1)).thenReturn(Collections.emptyList());
    // Act
    List<QuarterlySeason> qSeasons = qSeasonService.getBySeasonId(1);
    // Assert
    assertEquals(0, qSeasons.size());

    // Arrange
    when(qSeasonRepository.findBySeasonId(1))
        .thenReturn(Collections.singletonList(QuarterlySeason.builder().id(1).build()));
    // Act
    qSeasons = qSeasonService.getBySeasonId(1);
    // Assert
    assertEquals(1, qSeasons.size());
  }

  @Test
  public void getByDate() {
    // Arrange
    when(qSeasonRepository.findByDate(any())).thenReturn(Collections.emptyList());
    // Act and assert
    Assertions.assertThatThrownBy(() -> {
      qSeasonService.getByDate(LocalDate.now());
    }).isInstanceOf(NotFoundException.class)
        .hasMessageStartingWith("Could not find a quarterly for date");

    // Arrange
    when(qSeasonRepository.findByDate(any()))
        .thenReturn(Collections.singletonList(QuarterlySeason.builder().id(1).build()));
    // Act
    QuarterlySeason qSeason = qSeasonService.getByDate(LocalDate.now());

    // Assert
    assertEquals(1, qSeason.getId());
  }

  private void assertQuarterlySeason(QuarterlySeason qs) {
    switch (qs.getQuarter().getValue()) {
      case 1:
        // First day in May
        assertEquals(LocalDate.of(START_YEAR, Month.MAY.getValue(), 1), qs.getStart());
        assertEquals(LocalDate.of(START_YEAR, Month.AUGUST.getValue(), 1).minusDays(1),
            qs.getEnd());
        break;
      case 2:
        // First day in August
        assertEquals(LocalDate.of(START_YEAR, Month.AUGUST.getValue(), 1), qs.getStart());
        assertEquals(LocalDate.of(START_YEAR, Month.NOVEMBER.getValue(), 1).minusDays(1),
            qs.getEnd());
        break;
      case 3:
        // First day in November
        assertEquals(LocalDate.of(START_YEAR, Month.NOVEMBER.getValue(), 1), qs.getStart());
        assertEquals(LocalDate.of(START_YEAR + 1, Month.FEBRUARY.getValue(), 1).minusDays(1),
            qs.getEnd());
        break;
      case 4:
        // First day in February
        assertEquals(LocalDate.of(START_YEAR + 1, Month.FEBRUARY.getValue(), 1), qs.getStart());
        assertEquals(LocalDate.of(START_YEAR + 1, Month.MAY.getValue(), 1).minusDays(1),
            qs.getEnd());
        break;
    }

    assertEquals(13, qs.getNumGames());
    assertEquals(0, qs.getNumGamesPlayed());
    assertEquals(0, qs.getQTocCollected());
    assertFalse(qs.isFinalized());
    assertEquals(QUARTERLY_TOC_PER_GAME, qs.getQTocPerGameCost());
    assertEquals(QUARTERLY_NUM_PAYOUTS, qs.getNumPayouts());
    assertNull(qs.getPlayers());
    assertNull(qs.getPayouts());
  }
}