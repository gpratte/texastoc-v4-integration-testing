package com.texastoc.module.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationModuleFactory {

  private static NotificationModule NOTIFICATION_MODULE;

  public NotificationModuleFactory(NotificationModuleImpl notificationModule) {
    NOTIFICATION_MODULE = notificationModule;
  }

  /**
   * Return a concrete class that implements the NotificationModule interface
   *
   * @return a NotificationModule instance
   * @throws IllegalStateException if the NotificationModule instance is not ready
   */
  public static NotificationModule getNotificationModule() throws IllegalStateException {
    if (NOTIFICATION_MODULE == null) {
      throw new IllegalStateException("Game module instance not ready");
    }
    return NOTIFICATION_MODULE;
  }
}
