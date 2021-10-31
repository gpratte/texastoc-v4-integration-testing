package com.texastoc.exception;

public class PermissionDeniedException extends RuntimeException {

  public PermissionDeniedException() {
    super("Admin permission required for this action");
  }

  public PermissionDeniedException(String message) {
    super("Admin permission required for this action. " + message);
  }
}
