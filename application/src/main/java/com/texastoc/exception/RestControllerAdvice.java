package com.texastoc.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestControllerAdvice extends ResponseEntityExceptionHandler {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @ExceptionHandler(value = {BLException.class})
  protected ResponseEntity<Object> handleBLException(BLException blException, WebRequest request) {
    return super.handleExceptionInternal(blException, Response.builder()
        .code(blException.getStatus().name())
        .message(blException.getStatus().getReasonPhrase())
        .details(blException.getDetails())
        .build(), new HttpHeaders(), blException.getStatus(), request);
  }

  @ExceptionHandler(value = {RuntimeException.class})
  protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
    return super.handleExceptionInternal(ex, Response.builder()
        .code(HttpStatus.INTERNAL_SERVER_ERROR.name())
        .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .build(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    return super.handleExceptionInternal(ex, Response.builder()
        .code(status.name())
        .message(status.getReasonPhrase())
        .details(List.of(ErrorDetail.builder()
            .message("Message not readable")
            .build()))
        .build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    BindingResult bindingResult = ex.getBindingResult();
    log.info("!!! " + bindingResult);
    return this.handleExceptionInternal(ex, (Object) null, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    return super.handleExceptionInternal(ex, Response.builder()
        .code(status.name())
        .message(status.getReasonPhrase())
        .build(), headers, status, request);
  }

  @Builder
  @Data
  private static class Response {

    String code;
    String message;
    List<ErrorDetail> details;
  }
}
