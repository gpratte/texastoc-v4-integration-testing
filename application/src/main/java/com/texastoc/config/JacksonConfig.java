package com.texastoc.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure the global object mapper
 */
@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper buildObjectMapper() {
    // Do not include fields that have a null value
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}