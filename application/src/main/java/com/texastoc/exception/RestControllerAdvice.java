package com.texastoc.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestControllerAdvice extends ResponseEntityExceptionHandler {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final BLException SERVER_ERROR = new BLException(BLType.UNKNOWN);

  @ExceptionHandler(value = {BLException.class})
  protected ResponseEntity<Object> handleBLException(BLException blException) {
    return handleException(blException);
  }

  @ExceptionHandler(value = {RuntimeException.class})
  protected ResponseEntity<Object> handleRuntimeException() {
    return handleException(SERVER_ERROR);
  }

  private ResponseEntity<Object> handleException(BLException blException) {
    return new ResponseEntity<Object>(Response.builder()
        .correlationId(blException.getCorrelationId())
        .code(blException.getCode())
        .message(blException.getMessage())
        .details(blException.getDetails())
        .build(), blException.getStatus());
  }

  @Builder
  @Data
  private static class Response {

    UUID correlationId;
    String code;
    String message;
    ErrorDetails details;
  }
}
