package com.texastoc.module.game.exception;

public class GameInProgressException extends RuntimeException {

  public GameInProgressException() {
    super("Action cannot be completed because there is a game in progress");
  }
}
