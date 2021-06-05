package com.texastoc.config;

import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.notification.NotificationModuleFactory;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

  private PlayerModule playerModule;
  private NotificationModule notificationModule;

  /**
   * This event is executed as late as conceivably possible to indicate that
   * the application is ready to service requests.
   */
  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {

    // Get the admin that cares if the system started
    List<Player> players = getPlayerModule().getAll();
    for (Player player : players) {
      boolean isAdmin = false;
      for (Role role : player.getRoles()) {
        if (Role.Type.ADMIN == role.getType()) {
          isAdmin = true;
          break;
        }
      }

      if (isAdmin && !StringUtils.isBlank(player.getPhone()) && player.getFirstName().startsWith("Gil")) {
        log.info("sending application started text to " + player.getName());
        getNotificationModule().sendText(player.getPhone(), "texastoc started");
        break;
      }
    }
  }

  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

  private NotificationModule getNotificationModule() {
    if (notificationModule == null) {
      notificationModule = NotificationModuleFactory.getNotificationModule();
    }
    return notificationModule;
  }
}