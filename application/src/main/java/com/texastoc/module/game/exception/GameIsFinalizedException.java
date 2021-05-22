package com.texastoc.module.game.exception;

public class GameIsFinalizedException extends RuntimeException {

  public GameIsFinalizedException() {
    super("Action cannot be completed because the game is finalized");
  }
}
