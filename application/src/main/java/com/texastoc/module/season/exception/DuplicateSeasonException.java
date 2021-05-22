package com.texastoc.module.season.exception;

public class DuplicateSeasonException extends RuntimeException {

  public DuplicateSeasonException(int startYear) {
    super("A season that start in the year " + startYear + " already exists");
  }
}
