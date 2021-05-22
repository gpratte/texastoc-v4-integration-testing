package com.texastoc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.player.model.Player;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider JwtTokenProvider;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
      JwtTokenProvider JwtTokenProvider) {
    super.setAuthenticationFailureHandler(new JWTAuthenticationFailureHandler());
    this.authenticationManager = authenticationManager;
    this.JwtTokenProvider = JwtTokenProvider;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req,
      HttpServletResponse res) throws AuthenticationException {
    try {
      Player player = new ObjectMapper()
          .readValue(req.getInputStream(), Player.class);

      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              player.getEmail(),
              player.getPassword(),
              new ArrayList<>())
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain,
      Authentication auth) throws IOException, ServletException {
    final String token = JwtTokenProvider.generateToken(auth);

    // return token in body as json
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    res.getWriter().write("{\"token\":\"" + token + "\"}"
    );
  }

}
