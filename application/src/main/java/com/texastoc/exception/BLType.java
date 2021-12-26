package com.texastoc.exception;

import org.springframework.http.HttpStatus;

public enum BLType {

  BAD_REQUEST(HttpStatus.BAD_REQUEST, "INVALID DATA", "Invalid data"),
  DENIED(HttpStatus.FORBIDDEN, "UNAUTHORIZED", "Denied"),
  NOT_FOUND(HttpStatus.NOT_FOUND, "INVALID REQUEST", "Not found"),
  CONFLICT(HttpStatus.CONFLICT, "INVALID REQUEST", "Cannot fulfill request"),
  UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER ERROR", "Internal server error");

  private final HttpStatus status;
  private final String code;
  private final String message;

  BLType(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
