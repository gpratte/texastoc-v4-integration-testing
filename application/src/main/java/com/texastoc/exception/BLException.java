package com.texastoc.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * The one and only Business Logic Exception (BLException) that should be thrown. It encapulates the
 * type (including status), message and details.
 */
@Getter
@Setter
public class BLException extends RuntimeException {

  private UUID correlationId;
  private String code;
  private String message;
  private ErrorDetails details;
  @JsonIgnore
  private HttpStatus status;

  public BLException() {
  }

  public BLException(BLType blType) {
    message = blType.getMessage();
    code = blType.getCode();
    status = blType.getStatus();
  }

  public BLException(BLType blType, ErrorDetails details) {
    message = blType.getMessage();
    code = blType.getCode();
    status = blType.getStatus();
    this.details = details;
  }

  @Override
  public String toString() {
    return "{" +
        "code='" + code + '\'' +
        ", message=" + message +
        ", status=" + status +
        ", details=" + details +
        ", correlationId=" + correlationId +
        '}';
  }
}
