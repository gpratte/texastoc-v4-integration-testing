package com.texastoc.module.settings;

import com.texastoc.module.settings.service.SettingsService;
import org.springframework.stereotype.Component;

@Component
public class SettingsModuleFactory {

  private static SettingsModule SETTINGS_MODULE;

  public SettingsModuleFactory(SettingsService settingsService) {
    SETTINGS_MODULE = settingsService;
  }

  /**
   * Return a concrete class that implements the SettingsModule interface
   *
   * @return a SettingsModule instance
   * @throws IllegalStateException if the SettingsModule instance is not ready
   */
  public static SettingsModule getSettingsModule() throws IllegalStateException {
    if (SETTINGS_MODULE == null) {
      throw new IllegalStateException("Settings module instance not ready");
    }
    return SETTINGS_MODULE;
  }
}
