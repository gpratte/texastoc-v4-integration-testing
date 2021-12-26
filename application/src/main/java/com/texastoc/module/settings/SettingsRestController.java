package com.texastoc.module.settings;

import com.texastoc.module.settings.model.SystemSettings;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class SettingsRestController {

  private final SettingsModuleImpl settingsModule;

  public SettingsRestController(SettingsModuleImpl settingsModule) {
    this.settingsModule = settingsModule;
  }

  @GetMapping("/settings")
  @ResponseStatus(HttpStatus.OK)
  public SystemSettings get(HttpServletRequest request) {
    return settingsModule.get();
  }
}
