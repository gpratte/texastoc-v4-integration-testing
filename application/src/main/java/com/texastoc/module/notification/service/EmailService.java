package com.texastoc.module.notification.service;

import com.texastoc.module.notification.connector.EmailConnector;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final EmailConnector emailConnector;

  public EmailService(EmailConnector emailConnector) {
    this.emailConnector = emailConnector;
  }

  public void send(List<String> emails, String subject, String body) {
    emailConnector.send(emails, subject, body);
  }
}
