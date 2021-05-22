package com.texastoc.exception;

public class PermissionDeniedException extends RuntimeException {

  public PermissionDeniedException() {
    super("Admin permission required for this action");
  }
}
