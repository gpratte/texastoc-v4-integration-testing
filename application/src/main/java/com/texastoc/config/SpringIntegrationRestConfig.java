package com.texastoc.config;

import com.texastoc.module.game.event.GameEventProducer;
import com.texastoc.module.game.service.GameEventingRestImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ;;!! TODO
 */
@Configuration
@ConditionalOnProperty(prefix = "message", name = "rest")
public class SpringIntegrationRestConfig {

  @Bean
  public GameEventProducer gameEventProducer() {
    return new GameEventProducer(new GameEventingRestImpl());
  }
}
