package com.texastoc.security;

import static com.texastoc.security.SecurityConstants.HEADER_STRING;
import static com.texastoc.security.SecurityConstants.TOKEN_PREFIX;

import com.texastoc.module.player.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsServiceImpl userDetailsService;

  JwtAuthorizationFilter(AuthenticationManager authManager, JwtTokenProvider jwtTokenProvider,
      UserDetailsServiceImpl userDetailsService) {
    super(authManager);
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain) throws IOException, ServletException {
    String header = req.getHeader(HEADER_STRING);

    if (header == null || !header.startsWith(TOKEN_PREFIX)) {
      chain.doFilter(req, res);
      return;
    }

    UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(req, res);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

    String username = null;
    String jwtToken = null;
    String header = request.getHeader(HEADER_STRING);
    if (header != null && header.startsWith(TOKEN_PREFIX)) {
      jwtToken = header.replace(TOKEN_PREFIX, "");
      try {
        username = jwtTokenProvider.getUsernameFromToken(jwtToken);
      } catch (IllegalArgumentException e) {
        logger.error("an error occured during getting username from token", e);
      } catch (ExpiredJwtException e) {
        logger.warn("the token is expired and not valid anymore", e);
      } catch (SignatureException e) {
        logger.error("Authentication Failed. Username or Password not valid.");
      }
    } else {
      logger.warn("couldn't find bearer string, will ignore the header");
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (jwtTokenProvider.validateToken(jwtToken, userDetails)) {
        return jwtTokenProvider.getAuthentication(jwtToken, userDetails);
      }
    }

    return null;
  }

}
