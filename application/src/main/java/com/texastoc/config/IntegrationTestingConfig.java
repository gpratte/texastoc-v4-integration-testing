package com.texastoc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class IntegrationTestingConfig {

  private final boolean allowMultipleSeasons;

  public IntegrationTestingConfig(@Value("${test.allowMultipleSeasons:false}")
      boolean allowMultipleSeasons) {
    this.allowMultipleSeasons = allowMultipleSeasons;
  }
}
