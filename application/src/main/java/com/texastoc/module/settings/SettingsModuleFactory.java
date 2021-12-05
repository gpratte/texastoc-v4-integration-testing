package com.texastoc.module.settings;

import org.springframework.stereotype.Component;

@Component
public class SettingsModuleFactory {

  private static SettingsModule SETTINGS_MODULE;

  public SettingsModuleFactory(SettingsModuleImpl settingsModule) {
    SETTINGS_MODULE = settingsModule;
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
