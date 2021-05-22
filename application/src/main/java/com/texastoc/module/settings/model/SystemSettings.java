package com.texastoc.module.settings.model;

import java.util.List;
import java.util.Map;

public class SystemSettings extends Settings {

  private Map<Integer, List<Payout>> payouts;
  private Map<Integer, Map<Integer, Integer>> points;

  public SystemSettings() {
    super();
  }

  public SystemSettings(int id, Version version, Map<Integer, TocConfig> tocConfigs,
      Map<Integer, List<Payout>> payouts, Map<Integer, Map<Integer, Integer>> points) {
    super(id, version, tocConfigs);
    this.payouts = payouts;
    this.points = points;
  }

  public Map<Integer, List<Payout>> getPayouts() {
    return payouts;
  }

  public void setPayouts(
      Map<Integer, List<Payout>> payouts) {
    this.payouts = payouts;
  }

  public Map<Integer, Map<Integer, Integer>> getPoints() {
    return points;
  }

  public void setPoints(
      Map<Integer, Map<Integer, Integer>> points) {
    this.points = points;
  }
}
