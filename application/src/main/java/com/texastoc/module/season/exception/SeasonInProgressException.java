package com.texastoc.module.season.exception;

public class SeasonInProgressException extends RuntimeException {

  public SeasonInProgressException(int startYear) {
    super("A season that starts in the year " + startYear + " already exists");
  }

}
