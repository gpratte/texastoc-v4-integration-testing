package com.texastoc.module.settings;

import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettingsRestController implements SettingsModule {

  private final SettingsService settingsService;

  public SettingsRestController(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Override
  @GetMapping("/api/v4/settings")
  public SystemSettings get() {
    return settingsService.get();
  }

}
