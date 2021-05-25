package com.texastoc.module.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class SettingsIT extends BaseIntegrationTest {

  SystemSettings settings;

  @Before
  public void before() {
    settings = null;
  }

  @Test
  public void verifySettings() throws Exception {
    settings = getSettings();
    assertNotNull(settings);
    assertNotNull(settings.getVersion());

    assertNotNull(settings.getTocConfigs());
    TocConfig tocConfig = settings.getTocConfigs().get(2021);
    assertEquals(10, tocConfig.getKittyDebit());
    assertEquals(20, tocConfig.getAnnualTocCost());
    assertEquals(20, tocConfig.getQuarterlyTocCost());
    assertEquals(3, tocConfig.getQuarterlyNumPayouts());
    assertEquals(40, tocConfig.getRegularBuyInCost());
    assertEquals(40, tocConfig.getRegularRebuyCost());
    assertEquals(20, tocConfig.getRegularRebuyTocDebit());

    assertNotNull(settings.getPayouts());
    List<Payout> payouts = settings.getPayouts().get(2);
    Payout payout = payouts.get(0);
    assertEquals(1, payout.getPlace());
    assertEquals(0.65, payout.getPercent(), 0.0);
    payout = payouts.get(1);
    assertEquals(2, payout.getPlace());
    assertEquals(0.35, payout.getPercent(), 0.0);

    payouts = settings.getPayouts().get(3);
    payout = payouts.get(0);
    assertEquals(1, payout.getPlace());
    assertEquals(0.5, payout.getPercent(), 0.0);
    payout = payouts.get(1);
    assertEquals(2, payout.getPlace());
    assertEquals(0.3, payout.getPercent(), 0.0);
    payout = payouts.get(2);
    assertEquals(3, payout.getPlace());
    assertEquals(0.2, payout.getPercent(), 0.0);

    payouts = settings.getPayouts().get(10);
    payout = payouts.get(0);
    assertEquals(1, payout.getPlace());
    assertEquals(0.3, payout.getPercent(), 0.0);
    payout = payouts.get(1);
    assertEquals(2, payout.getPlace());
    assertEquals(0.19, payout.getPercent(), 0.0);
    payout = payouts.get(2);
    assertEquals(3, payout.getPlace());
    assertEquals(0.1325, payout.getPercent(), 0.0);
    payout = payouts.get(3);
    assertEquals(4, payout.getPlace());
    assertEquals(0.105, payout.getPercent(), 0.0);
    payout = payouts.get(4);
    assertEquals(5, payout.getPlace());
    assertEquals(0.075, payout.getPercent(), 0.0);
    payout = payouts.get(5);
    assertEquals(6, payout.getPlace());
    assertEquals(0.055, payout.getPercent(), 0.0);
    payout = payouts.get(6);
    assertEquals(7, payout.getPlace());
    assertEquals(0.0375, payout.getPercent(), 0.0);
    payout = payouts.get(7);
    assertEquals(8, payout.getPlace());
    assertEquals(0.03, payout.getPercent(), 0.0);
    payout = payouts.get(8);
    assertEquals(9, payout.getPlace());
    assertEquals(0.0225, payout.getPercent(), 0.0);
    payout = payouts.get(9);
    assertEquals(10, payout.getPlace());
    assertEquals(0.015, payout.getPercent(), 0.0);
  }
}
