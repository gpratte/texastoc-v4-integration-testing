package com.texastoc.module.season.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.repository.SeasonHistoryRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HistoricalSeasonService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SeasonHistoryRepository seasonHistoryRepository;

  public HistoricalSeasonService(SeasonHistoryRepository seasonHistoryRepository) {
    this.seasonHistoryRepository = seasonHistoryRepository;
  }

  public List<HistoricalSeason> getPastSeasons() {
    return seasonHistoryRepository.findByOrderByStartYearDesc();
  }

}
