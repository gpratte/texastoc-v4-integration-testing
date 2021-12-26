package com.texastoc.module.settings;

import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.service.SettingsService;
import org.springframework.stereotype.Service;

@Service
public class SettingsModuleImpl implements SettingsModule {

  private final SettingsService settingsService;

  public SettingsModuleImpl(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Override
  public SystemSettings get() {
    return settingsService.get();
  }
}
