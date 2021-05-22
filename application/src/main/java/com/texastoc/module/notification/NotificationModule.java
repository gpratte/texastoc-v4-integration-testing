package com.texastoc.module.notification;

import java.util.List;

public interface NotificationModule {

  /**
   * Send an email
   *
   * @param emails
   * @param subject
   * @param body
   */
  public void sendEmail(List<String> emails, String subject, String body);

  /**
   * Send a text message
   *
   * @param phone   the phone
   * @param message the message
   */
  public void sendText(String phone, String message);
}
