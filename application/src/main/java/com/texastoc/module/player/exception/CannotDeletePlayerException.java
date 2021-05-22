package com.texastoc.module.player.exception;

public class CannotDeletePlayerException extends RuntimeException {

  public CannotDeletePlayerException(int playerId) {
    super("Player with ID " + playerId + " cannot be deleted");
  }
}
