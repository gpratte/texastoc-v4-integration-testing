package com.texastoc.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestControllerAdvise extends ResponseEntityExceptionHandler {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(), "Validation Failed",
        ex.getBindingResult().toString());
    return new ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST);
  }

  // TODO just have one Exception type

  @ExceptionHandler(value = {EmptyResultDataAccessException.class})
  protected ResponseEntity<Object> handleEmptyResultDataAccessException(
      EmptyResultDataAccessException ex, HttpServletRequest request) {
    return handleException(null, null, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(value = {AccessDeniedException.class})
  protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex,
      HttpServletRequest request) {
    return handleException(null, null, HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler(value = {PermissionDeniedException.class})
  protected ResponseEntity<Object> handleAccessDenied(PermissionDeniedException ex,
      HttpServletRequest request) {
    return handleException(null, null, HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler(value = {IncorrectResultSizeDataAccessException.class})
  protected ResponseEntity<Object> handleIncorrectSize(IncorrectResultSizeDataAccessException ex,
      HttpServletRequest request) {
    return handleException(null, null, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(value = {NotFoundException.class})
  protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex,
      HttpServletRequest request) {
    return handleException(null, null, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(value = {JsonProcessingException.class})
  protected ResponseEntity<Object> handleJsonProcessingException(JsonProcessingException ex,
      HttpServletRequest request) {
    return handleException(ex, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler(value = {RuntimeException.class})
  protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex,
      HttpServletRequest request) {
    log.info("request " + request);
    return handleException(ex, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private ResponseEntity<Object> handleException(Exception e, Object body, HttpStatus status,
      HttpServletRequest request) {
    if (e != null) {
      if ("chaos".equals(e.getMessage())) {
        log.error("chaos");
      } else {
        log.error(e.getMessage(), e);
      }
    }
    try {
      ObjectNode response = OBJECT_MAPPER.createObjectNode();
      ObjectNode data = OBJECT_MAPPER.createObjectNode();
      response.set("response", data);
      data.put("correlationId", MDC.get("correlationId"));
      data.put("uri", request.getRequestURI());
      data.put("status", status.value());
      if (body != null) {
        data.put("body", body.toString());
      }
      log.info(OBJECT_MAPPER.writeValueAsString(response));
    } catch (Exception ex) {
      log.error("Could not log the response", ex);
    } finally {
      MDC.clear();
    }
    return new ResponseEntity<Object>(body, status);
  }


}
