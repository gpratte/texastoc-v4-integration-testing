package com.texastoc.security;

import static com.texastoc.common.LoggingFilter.CORRELATION_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.player.model.Player;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Order(1)
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider JwtTokenProvider;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
      JwtTokenProvider JwtTokenProvider) {
    super.setAuthenticationFailureHandler(new JWTAuthenticationFailureHandler());
    this.authenticationManager = authenticationManager;
    this.JwtTokenProvider = JwtTokenProvider;
    this.setFilterProcessesUrl("/api/v4/login");
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req,
      HttpServletResponse res) throws AuthenticationException {
    try {
      Player player = new ObjectMapper().readValue(req.getInputStream(), Player.class);

      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              player.getEmail(),
              player.getPassword(),
              new ArrayList<>())
      );
    } catch (AuthenticationException e) {
      throw e;
    } catch (RuntimeException | IOException e) {
      log.error("Error authenticating", e);
      throw new BadCredentialsException("bad credentials");
    }
  }

  // Same as the doFilter method in LoggingFiler
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    if (!"/api/v4/login".equals(req.getRequestURI())) {
      super.doFilter(req, res, chain);
      return;
    }

    final String correlationId =
        req.getHeader(CORRELATION_ID) != null ? req.getHeader(CORRELATION_ID)
            : UUID.randomUUID().toString();

    if (req.getHeader(CORRELATION_ID) == null) {
      // No CORRELATION_ID header so provide it
      req = new HttpServletRequestWrapper((HttpServletRequest) request) {
        @Override
        public String getHeader(String name) {
          if (CORRELATION_ID.equals(name)) {
            return correlationId;
          }
          return super.getHeader(name);
        }
      };
    }

    res.setHeader(CORRELATION_ID, correlationId);

    log.info(
        "request: correlationId={} action={} uri={} contentType={}",
        req.getHeader(CORRELATION_ID),
        req.getMethod(),
        req.getRequestURI(),
        req.getContentType());

    super.doFilter(req, res, chain);

    log.info("response: correlationId={} action={} uri={} status={}",
        req.getHeader(CORRELATION_ID),
        req.getMethod(),
        req.getRequestURI(),
        res.getStatus());
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain,
      Authentication auth) throws IOException, ServletException {
    log.info("response: action={} uri={} status={}",
        req.getMethod(),
        req.getRequestURI(),
        res.getStatus());
    final String token = JwtTokenProvider.generateToken(auth);

    // return token in body as json
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    res.getWriter().write("{\"token\":\"" + token + "\"}"
    );
  }

}
