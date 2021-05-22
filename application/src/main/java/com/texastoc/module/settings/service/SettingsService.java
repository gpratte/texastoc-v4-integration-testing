package com.texastoc.module.settings.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.common.PointsGenerator;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.repository.SettingsRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SettingsService implements SettingsModule {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SettingsRepository settingsRepository;
  private final String payoutsFileName;

  private Map<Integer, List<Payout>> payouts;
  private Map<Integer, Map<Integer, Integer>> points;

  public SettingsService(SettingsRepository settingsRepository, @Value("${payouts.fileName}")
      String payoutsFileName, PointsGenerator pointsGenerator) {
    this.settingsRepository = settingsRepository;
    this.payoutsFileName = payoutsFileName;

    try {
      payouts = OBJECT_MAPPER
          .readValue(getPayoutsAsJson(), new TypeReference<Map<Integer, List<Payout>>>() {
          });
    } catch (Exception e) {
      log.warn("Could not process payouts json", e);
      payouts = new HashMap<>();
    }

    points = new HashMap<>();
    for (int i = 2; i <= 50; i++) {
      points.put(i, pointsGenerator.generatePlacePoints(i));
    }
  }

  // TODO cache
  @Override
  public SystemSettings get() {
    Settings settings = settingsRepository.findById(1).get();
    return new SystemSettings(settings.getId(), settings.getVersion(), settings.getTocConfigs(),
        payouts, points);
  }

  private String getPayoutsAsJson() throws IOException {
    InputStream inputStream = new ClassPathResource(payoutsFileName).getInputStream();
    try (BufferedReader bf = new BufferedReader(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return bf.lines().collect(Collectors.joining());
    }
  }
}