package com.texastoc.security;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class JWTAuthenticationFailureHandler implements AuthenticationFailureHandler {

  private static final String ERROR_JSON = "{\"timestamp\": %d, "
      + "\"status\": 401, "
      + "\"error\": \"Unauthorized\", "
      + "\"message\": \"Authentication failed: bad credentials\", "
      + "\"path\": \"/api/v4/login\"}";

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException e) throws IOException, ServletException {
    response.setStatus(401);
    response.setContentType("application/json");
    response.getWriter().append(String.format(ERROR_JSON, new Date().getTime()));
  }
}
