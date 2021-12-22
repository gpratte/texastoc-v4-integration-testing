package com.texastoc.common;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

/**
 * May be better suited to an API gateway than a servlet filter.
 * <p>Logs http requests/responses</p>
 */
@Order(1)
@Slf4j
public class LoggingFilter implements Filter {

  public static final String CORRELATION_ID = "CORRELATION_ID";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    if (req.getHeader(CORRELATION_ID) == null) {
      // No CORRELATION_ID header so provide it
      String correlationId = UUID.randomUUID().toString();
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

    log.info(
        "request: correlationId={} action={} uri={} contentType={}",
        req.getHeader(CORRELATION_ID),
        req.getMethod(),
        req.getRequestURI(),
        req.getContentType());

    chain.doFilter(req, res);

    log.info("response: correlationId={} action={} uri={} status={}",
        req.getHeader(CORRELATION_ID),
        req.getMethod(),
        req.getRequestURI(),
        res.getStatus());
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Filter.super.init(filterConfig);
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }
}
