package com.texastoc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.exception.BLException;
import org.springframework.web.client.HttpClientErrorException;

public class TestUtils implements TestConstants {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static BLException convert(HttpClientErrorException exception) {
    String body = exception.getResponseBodyAsString();
    System.out.println(body);
    try {
      return OBJECT_MAPPER.readValue(exception.getResponseBodyAsString(), BLException.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

}
