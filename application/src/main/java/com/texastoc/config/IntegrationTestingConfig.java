package com.texastoc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class IntegrationTestingConfig {

  private final boolean allowMultipleSeasons;
  private final boolean allowChaos;

  public IntegrationTestingConfig(
      @Value("${test.allowMultipleSeasons:false}") boolean allowMultipleSeasons,
      @Value("${test.allowChaos:false}") boolean allowChaos) {
    this.allowMultipleSeasons = allowMultipleSeasons;
    this.allowChaos = allowChaos;
  }
}
