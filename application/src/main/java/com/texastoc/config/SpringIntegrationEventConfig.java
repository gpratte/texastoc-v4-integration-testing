package com.texastoc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:game-pub-sub.xml")
public class SpringIntegrationEventConfig {

}
