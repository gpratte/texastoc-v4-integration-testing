package com.texastoc.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
//@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static long THIRTY_MINUTES_MILLISECONDS = 30 * 60 * 1000;

  //@Autowired
  private WebSocketMessageBrokerStats webSocketMessageBrokerStats;

  @PostConstruct
  public void init() {
    //webSocketMessageBrokerStats.setLoggingPeriod(THIRTY_MINUTES_MILLISECONDS);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/socket").setAllowedOriginPatterns("*");
    registry.addEndpoint("/socket").setAllowedOriginPatterns("*").withSockJS();
  }
}