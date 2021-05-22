package com.texastoc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:game-pub-sub.xml")
@ConditionalOnProperty(prefix = "message", name = "events")
public class SpringIntegrationEventConfig {

}
