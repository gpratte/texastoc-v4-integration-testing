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

  public IntegrationTestingConfig(
      @Value("${test.allowMultipleSeasons:false}") boolean allowMultipleSeasons,
      @Value("${test.allowChaos:false}") boolean allowChaos) {
    log.info("allowMultipleSeasons: {}", allowMultipleSeasons);
    log.info("allowChaos: {}", allowChaos);
    this.allowMultipleSeasons = allowMultipleSeasons;
    this.allowChaos = allowChaos;
  }
}
