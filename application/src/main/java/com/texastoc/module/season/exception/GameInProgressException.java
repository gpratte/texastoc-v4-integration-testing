package com.texastoc.module.season.exception;

public class GameInProgressException extends RuntimeException {

  public GameInProgressException(String message) {
    super(message);
  }
}
