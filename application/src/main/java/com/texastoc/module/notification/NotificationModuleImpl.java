package com.texastoc.module.notification;

import com.texastoc.module.notification.connector.SMSConnector;
import com.texastoc.module.notification.service.EmailService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationModuleImpl implements NotificationModule {

  private final EmailService emailService;
  private final SMSConnector smsConnector;

  public NotificationModuleImpl(EmailService emailService,
      SMSConnector smsConnector) {
    this.emailService = emailService;
    this.smsConnector = smsConnector;
  }

  @Override
  public void sendEmail(List<String> emails, String subject, String body) {
    emailService.send(emails, subject, body);
  }

  @Override
  public void sendText(String phone, String message) {
    smsConnector.text(phone, message);
  }
}
