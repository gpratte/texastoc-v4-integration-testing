package com.texastoc;

import com.texastoc.module.season.model.SeasonPayoutPlace;
import com.texastoc.module.season.model.SeasonPayoutRange;
import com.texastoc.module.season.model.SeasonPayoutSettings;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface TestConstants {

  int KITTY_PER_GAME = 9;
  int TOC_PER_GAME = 8;
  int QUARTERLY_TOC_PER_GAME = 7;
  int QUARTERLY_NUM_PAYOUTS = 3;

  int GAME_BUY_IN = 6;
  int GAME_REBUY = 5;
  int GAME_REBUY_TOC_DEBIT = 4;

  double CHOP_TENTH_PLACE_INCR = 0.5;
  int CHOP_TENTH_PLACE_POINTS = 3;
  double CHOP_MULTIPLIER = 1.291;

  static TocConfig getTocConfig() {
    return TocConfig.builder()
        .kittyDebit(KITTY_PER_GAME)
        .annualTocCost(TOC_PER_GAME)
        .quarterlyTocCost(QUARTERLY_TOC_PER_GAME)
        .quarterlyNumPayouts(QUARTERLY_NUM_PAYOUTS)
        .regularBuyInCost(GAME_BUY_IN)
        .regularRebuyCost(GAME_REBUY)
        .regularRebuyTocDebit(GAME_REBUY_TOC_DEBIT)
        .build();
  }

  static List<Payout> getPayouts(int num) {
    Map<Integer, List<Payout>> payoutsMap = new HashMap<>();
    List<Payout> payouts = new ArrayList<>(2);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.65)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.35)
        .build());
    payoutsMap.put(2, payouts);

    payouts = new ArrayList<>(3);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.50)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.30)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.20)
        .build());
    payoutsMap.put(3, payouts);

    payouts = new ArrayList<>(4);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.45)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.25)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.18)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.12)
        .build());
    payoutsMap.put(4, payouts);

    payouts = new ArrayList<>(5);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.40)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.23)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.16)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.12)
        .build());
    payouts.add(Payout.builder()
        .place(5)
        .percent(0.09)
        .build());
    payoutsMap.put(5, payouts);

    payouts = new ArrayList<>(6);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.38)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.22)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.15)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.11)
        .build());
    payouts.add(Payout.builder()
        .place(5)
        .percent(0.08)
        .build());
    payouts.add(Payout.builder()
        .place(6)
        .percent(0.06)
        .build());
    payoutsMap.put(6, payouts);

    payouts = new ArrayList<>(7);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.35)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.21)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.15)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.11)
        .build());
    payouts.add(Payout.builder()
        .place(5)
        .percent(0.08)
        .build());
    payouts.add(Payout.builder()
        .place(6)
        .percent(0.06)
        .build());
    payouts.add(Payout.builder()
        .place(7)
        .percent(0.04)
        .build());
    payoutsMap.put(7, payouts);

    payouts = new ArrayList<>(8);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.335)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.20)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.145)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.11)
        .build());
    payouts.add(Payout.builder()
        .place(5)
        .percent(0.08)
        .build());
    payouts.add(Payout.builder()
        .place(6)
        .percent(0.06)
        .build());
    payouts.add(Payout.builder()
        .place(7)
        .percent(0.04)
        .build());
    payouts.add(Payout.builder()
        .place(8)
        .percent(0.03)
        .build());
    payoutsMap.put(8, payouts);

    payouts = new ArrayList<>(9);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.32)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.195)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.14)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.11)
        .build());
    payouts.add(Payout.builder()
        .place(5)
        .percent(0.08)
        .build());
    payouts.add(Payout.builder()
        .place(6)
        .percent(0.06)
        .build());
    payouts.add(Payout.builder()
        .place(7)
        .percent(0.04)
        .build());
    payouts.add(Payout.builder()
        .place(8)
        .percent(0.03)
        .build());
    payouts.add(Payout.builder()
        .place(9)
        .percent(0.025)
        .build());
    payoutsMap.put(9, payouts);

    payouts = new ArrayList<>(10);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.30)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.19)
        .build());
    payouts.add(Payout.builder()
        .place(3)
        .percent(0.1325)
        .build());
    payouts.add(Payout.builder()
        .place(4)
        .percent(0.105)
        .build());
    payouts.add(Payout.builder()
        .place(5)
        .percent(0.075)
        .build());
    payouts.add(Payout.builder()
        .place(6)
        .percent(0.055)
        .build());
    payouts.add(Payout.builder()
        .place(7)
        .percent(0.0375)
        .build());
    payouts.add(Payout.builder()
        .place(8)
        .percent(0.03)
        .build());
    payouts.add(Payout.builder()
        .place(9)
        .percent(0.0225)
        .build());
    payouts.add(Payout.builder()
        .place(10)
        .percent(0.015)
        .build());
    payoutsMap.put(10, payouts);

    return payoutsMap.get(num);
  }

  static SystemSettings getSettings() {
    Map<Integer, TocConfig> tocMap = new HashMap<>();
    tocMap.put(2020, getTocConfig());

    SystemSettings settings = new SystemSettings();
    settings.setPayouts(TestUtils.getPayouts());
    settings.setTocConfigs(tocMap);
    return settings;
  }

  static SeasonPayoutSettings getSeasonPayoutSettings(int startYear) {
    SeasonPayoutSettings seasonPayoutSettings = new SeasonPayoutSettings();
    seasonPayoutSettings.setStartYear(startYear);

    List<SeasonPayoutRange> ranges = new LinkedList<>();
    seasonPayoutSettings.setRanges(ranges);

    SeasonPayoutRange seasonPayoutRange = new SeasonPayoutRange();
    ranges.add(seasonPayoutRange);
    seasonPayoutRange.setLowRange(3000);
    seasonPayoutRange.setHighRange(5000);

    List<SeasonPayoutPlace> guaranteed = new LinkedList<>();
    seasonPayoutRange.setGuaranteed(guaranteed);
    SeasonPayoutPlace seasonPayoutPlace = new SeasonPayoutPlace();
    guaranteed.add(seasonPayoutPlace);
    seasonPayoutPlace.setPlace(1);
    seasonPayoutPlace.setAmount(700);
    seasonPayoutPlace.setPercent(20);

    List<SeasonPayoutPlace> finalTable = new LinkedList<>();
    seasonPayoutRange.setFinalTable(finalTable);

    seasonPayoutPlace = new SeasonPayoutPlace();
    finalTable.add(seasonPayoutPlace);
    seasonPayoutPlace.setPlace(2);
    seasonPayoutPlace.setAmount(600);
    seasonPayoutPlace.setPercent(20);

    seasonPayoutPlace = new SeasonPayoutPlace();
    finalTable.add(seasonPayoutPlace);
    seasonPayoutPlace.setPlace(3);
    seasonPayoutPlace.setAmount(450);
    seasonPayoutPlace.setPercent(16);

    seasonPayoutPlace = new SeasonPayoutPlace();
    finalTable.add(seasonPayoutPlace);
    seasonPayoutPlace.setPlace(4);
    seasonPayoutPlace.setAmount(350);
    seasonPayoutPlace.setPercent(14);

    seasonPayoutPlace = new SeasonPayoutPlace();
    finalTable.add(seasonPayoutPlace);
    seasonPayoutPlace.setPlace(5);
    seasonPayoutPlace.setAmount(0);
    seasonPayoutPlace.setPercent(30);

    return seasonPayoutSettings;
  }
}
