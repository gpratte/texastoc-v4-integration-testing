package com.texastoc.module.notification.connector;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailConnector {

  private static final int TIMEOUT = 10000;
  private static RequestConfig requestConfig;

  private final ExecutorService executorService;
  private final String apiKey;

  public EmailConnector(@Value("${postmarkapp.key:POSTMARK_API_TEST}") String apiKey) {
    this.apiKey = apiKey;
    executorService = Executors.newCachedThreadPool();
  }

  public void send(List<String> emails, String subject, String body) {
    if (emails != null && subject != null && body != null) {
      emails.forEach(email -> this.send(email, subject, body));
    }
  }

  private void send(String email, String subject, String body) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("From: 'info@texastoc.com',");
    sb.append("To: '" + email + "',");
    sb.append("Subject: '" + subject + "',");
    sb.append("HtmlBody: '");
    sb.append(body);
    sb.append("'");
    sb.append("}");

    executorService.submit(new EmailSender(sb.toString()));
  }

  private class EmailSender implements Callable<Void> {

    private final String emailPayload;

    public EmailSender(String emailPayload) {
      this.emailPayload = emailPayload;
    }

    @Override
    public Void call() throws Exception {
      try {
        CloseableHttpClient client = HttpClientBuilder
            .create()
            .setDefaultRequestConfig(httpClientConfig())
            .build();
        HttpPost post = new HttpPost("https://api.postmarkapp.com/email");
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/json");
        post.setHeader("X-Postmark-Server-Token", apiKey);
        StringEntity payload = new StringEntity(emailPayload, "UTF-8");
        post.setEntity(payload);

        try {
          client.execute(post);
        } catch (HttpResponseException hre) {
          switch (hre.getStatusCode()) {
            case 401:
            case 422:
              log.warn("There was a problem with the email: "
                  + hre.getMessage());
              break;
            case 500:
              log.warn("There has been an error sending your email: "
                  + hre.getMessage());
              break;
            default:
              log.warn("There has been an unknown error sending your email: "
                  + hre.getMessage());
          }
        }
      } catch (Exception e) {
        log.error("Could not send email " + e);
      }
      return null;
    }
  }

  private static RequestConfig httpClientConfig() {
    if (requestConfig == null) {
      requestConfig = RequestConfig.custom()
          .setConnectTimeout(TIMEOUT)
          .setConnectionRequestTimeout(TIMEOUT)
          .setSocketTimeout(TIMEOUT)
          .build();
    }
    return requestConfig;
  }
}
