package com.texastoc.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@Component
public class IntegrationTestingConfig {

  private final boolean allowMultipleSeasons;
  private final boolean allowChaos;
  private final int chaosFrequency;

  public IntegrationTestingConfig(
      @Value("${test.allowMultipleSeasons:false}") boolean allowMultipleSeasons,
      @Value("${test.allowChaos:false}") boolean allowChaos,
      @Value("${test.chaosFrequency:10}") int chaosFrequency) {
    log.info("allowMultipleSeasons: {}", allowMultipleSeasons);
    log.info("allowChaos: {}", allowChaos);
    log.info("chaosFrequency: {}", chaosFrequency);
    this.allowMultipleSeasons = allowMultipleSeasons;
    this.allowChaos = allowChaos;
    this.chaosFrequency = chaosFrequency;
  }
}
