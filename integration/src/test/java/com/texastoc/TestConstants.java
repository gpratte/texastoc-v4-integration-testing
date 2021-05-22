package com.texastoc;

import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.TocConfig;
import java.util.ArrayList;
import java.util.List;

public interface TestConstants {

  int KITTY_PER_GAME = 10;
  int TOC_PER_GAME = 20;
  int QUARTERLY_TOC_PER_GAME = 20;
  int QUARTERLY_NUM_PAYOUTS = 3;

  int GAME_BUY_IN = 40;
  int GAME_REBUY = 40;
  int GAME_REBUY_TOC_DEBIT = 20;

  int GIL_PRATTE_PLAYER_ID = 1;
  String GIL_PRATTE_NAME = "Gil Pratte";
  int GUEST_USER_PLAYER_ID = 2;
  String GUEST_USER_NAME = "Guest User";

  int CHOP_NUM_PLAYERS = 2;
  double CHOP_TENTH_PLACE_INCR = 0.5;
  int CHOP_TENTH_PLACE_POINTS = 3;
  double CHOP_MULTIPLIER = 1.291;

  String ADMIN_EMAIL = "gilpratte@texastoc.com";
  String ADMIN_PASSWORD = "password";
  String USER_EMAIL = "guest@texastoc.com";
  String USER_PASSWORD = "password";

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

    if (num == 2) {
      List<Payout> payouts = new ArrayList<>(2);
      payouts.add(Payout.builder()
          .place(1)
          .percent(0.65)
          .build());
      payouts.add(Payout.builder()
          .place(2)
          .percent(0.35)
          .build());
      return payouts;
    } else if (num == 3) {
      List<Payout> payouts = new ArrayList<>(3);
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
      return payouts;
    } else if (num == 4) {
      List<Payout> payouts = new ArrayList<>(4);
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
      return payouts;
    } else if (num == 5) {
      List<Payout> payouts = new ArrayList<>(5);
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
      return payouts;
    } else if (num == 6) {
      List<Payout> payouts = new ArrayList<>(6);
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
      return payouts;
    } else if (num == 7) {
      List<Payout> payouts = new ArrayList<>(7);
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
      return payouts;
    } else if (num == 8) {
      List<Payout> payouts = new ArrayList<>(8);
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
      return payouts;
    } else if (num == 9) {
      List<Payout> payouts = new ArrayList<>(9);
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
      return payouts;
    } else if (num == 10) {
      List<Payout> payouts = new ArrayList<>(10);
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
      return payouts;
    }

    throw new RuntimeException("no payouts found for " + num);
  }


}
