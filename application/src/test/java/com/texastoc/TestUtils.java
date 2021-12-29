package com.texastoc;

import static org.assertj.core.api.Assertions.assertThat;

import com.texastoc.exception.BLException;
import com.texastoc.exception.ErrorDetails;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.TocConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class TestUtils implements TestConstants {

  private static final Map<Integer, List<Payout>> PAYOUTS;

  public static void verifyBLException(BLException blException, HttpStatus status) {
    verifyBLException(blException, status, null);
  }

  public static void verifyBLException(BLException blException, HttpStatus status,
      ErrorDetails errorDetails) {
    assertThat(blException.getStatus()).isEqualTo(status);
    assertThat(blException.getDetails()).isEqualTo(errorDetails);
  }

  public static void populateGameCosts(Game game) {
    TocConfig tocConfig = TestConstants.getTocConfig();
    game.setAnnualTocCost(tocConfig.getAnnualTocCost());
    game.setQuarterlyTocCost(tocConfig.getQuarterlyTocCost());
    game.setKittyCost(tocConfig.getKittyDebit());
    game.setBuyInCost(tocConfig.getRegularBuyInCost());
    game.setRebuyAddOnCost(tocConfig.getRegularRebuyCost());
    game.setRebuyAddOnTocDebitCost(tocConfig.getRegularRebuyTocDebit());
  }

  static Map<Integer, List<Payout>> getPayouts() {
    return PAYOUTS;
  }

  static {
    PAYOUTS = new HashMap<>();

    List<Payout> payouts = new ArrayList<>(2);
    payouts.add(Payout.builder()
        .place(1)
        .percent(0.65)
        .build());
    payouts.add(Payout.builder()
        .place(2)
        .percent(0.35)
        .build());
    PAYOUTS.put(2, payouts);

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
    PAYOUTS.put(3, payouts);

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
    PAYOUTS.put(4, payouts);

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
    PAYOUTS.put(5, payouts);

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
    PAYOUTS.put(6, payouts);

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
    PAYOUTS.put(7, payouts);

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
    PAYOUTS.put(8, payouts);

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
    PAYOUTS.put(9, payouts);

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
    PAYOUTS.put(10, payouts);
  }
}
