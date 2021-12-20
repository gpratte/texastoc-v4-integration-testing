package com.texastoc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.player.model.Player;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    log.info(
        "request: action={} uri={} contentType={}",
        req.getMethod(),
        req.getRequestURI(),
        req.getContentType());

    try {
      Player player = new ObjectMapper().readValue(req.getInputStream(), Player.class);

      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              player.getEmail(),
              player.getPassword(),
              new ArrayList<>())
      );
    } catch (AuthenticationException e) {
      log.info("response: action={} uri={} status={}",
          req.getMethod(),
          req.getRequestURI(),
          401);
      throw e;
    } catch (Exception e) {
      log.info("response: action={} uri={} status={}",
          req.getMethod(),
          req.getRequestURI(),
          401);
      throw new BadCredentialsException("bad credentials");
    }
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
