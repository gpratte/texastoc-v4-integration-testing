package com.texastoc.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ErrorDetails {

  private String target;
  private String message;

  @Override
  public String toString() {
    return "{" +
        "target='" + target + '\'' +
        ", message='" + message + '\'' +
        '}';
  }
}
