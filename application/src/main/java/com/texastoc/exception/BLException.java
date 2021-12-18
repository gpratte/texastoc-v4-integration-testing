package com.texastoc.exception;

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

  private String code;
  private HttpStatus status;
  private ErrorDetails details;
  private UUID correlationId;

  public BLException(BLType blType) {
    super(blType.getMessage());
    code = blType.getCode();
    status = blType.getStatus();
  }

  public BLException(BLType blType, ErrorDetails details) {
    super(blType.getMessage());
    this.code = blType.getCode();
    this.status = blType.getStatus();
    this.details = details;
  }

  @Override
  public String toString() {
    return "{" +
        "code='" + code + '\'' +
        ", status=" + status +
        ", details=" + details +
        ", correlationId=" + correlationId +
        '}';
  }
}
