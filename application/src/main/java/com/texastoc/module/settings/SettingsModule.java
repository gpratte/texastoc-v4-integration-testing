package com.texastoc.module.settings;

import com.texastoc.module.settings.model.SystemSettings;

public interface SettingsModule {

  /**
   * Get all settings
   *
   * @return
   */
  SystemSettings get();
}
