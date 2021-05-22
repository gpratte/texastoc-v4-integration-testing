package com.texastoc.module.season.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.repository.SeasonHistoryRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HistoricalSeasonService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SeasonHistoryRepository seasonHistoryRepository;

  private String pastSeasonsAsJson = null;

  public HistoricalSeasonService(SeasonHistoryRepository seasonHistoryRepository) {
    this.seasonHistoryRepository = seasonHistoryRepository;
  }

  public List<HistoricalSeason> getPastSeasons() {
    List<HistoricalSeason> historicalSeasonsFromJson = null;
    String json = getPastSeasonsAsJson();
    try {
      historicalSeasonsFromJson = OBJECT_MAPPER
          .readValue(json, new TypeReference<List<HistoricalSeason>>() {
          });
    } catch (JsonProcessingException e) {
      log.warn("Could not deserialize historical seasons json");
      historicalSeasonsFromJson = new LinkedList<>();
    }

    List<HistoricalSeason> historicalSeasons = StreamSupport
        .stream(seasonHistoryRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());

    historicalSeasons.addAll(historicalSeasonsFromJson);
    return historicalSeasons;
  }

  private String getPastSeasonsAsJson() {
    if (pastSeasonsAsJson != null) {
      return pastSeasonsAsJson;
    }
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("season_history.json").getInputStream();
    } catch (IOException e) {
      return null;
    }
    try (BufferedReader bf = new BufferedReader(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      pastSeasonsAsJson = bf.lines().collect(Collectors.joining());
      return pastSeasonsAsJson;
    } catch (IOException e) {
      return null;
    }
  }
}
