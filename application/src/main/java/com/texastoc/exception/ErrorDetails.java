package com.texastoc.exception;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ErrorDetails {

  private String target;
  private String message;
  private List<String> details;

  @Override
  public String toString() {
    return "{" +
        "target='" + target + '\'' +
        ", message='" + message + '\'' +
        ", details=" + details +
        '}';
  }
}
