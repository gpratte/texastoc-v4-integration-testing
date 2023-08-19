package com.texastoc.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * The one and only Business Logic Exception (BLException) that should be thrown. It encapsulates
 * the type (including status), message and details.
 */
@Getter
@Setter
public class BLException extends RuntimeException {

  private String code;
  private String message;
  private List<ErrorDetail> details;
  @JsonIgnore
  private HttpStatus status;

  public BLException() {
  }

  public BLException(HttpStatus status) {
    this(status, null);
  }

  public BLException(HttpStatus status, List<ErrorDetail> details) {
    this.status = status;
    code = status.name();
    message = status.getReasonPhrase();
    this.details = details;
  }

  @Override
  public String toString() {
    return "{code=" + code + " message=" + message + " details=" + details + "}";
  }
}
