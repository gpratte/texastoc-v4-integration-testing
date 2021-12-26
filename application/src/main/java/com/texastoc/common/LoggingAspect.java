package com.texastoc.common;

import static com.texastoc.common.LoggingFilter.CORRELATION_ID;

import com.texastoc.exception.BLException;
import java.lang.reflect.Method;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Logs module requests/responses
 */
@Aspect
@Order(1)
@Slf4j
@Component
public class LoggingAspect {

  @Around("bean(*Controller)")
  public Object aroundControllerAdvice(ProceedingJoinPoint pjp) throws Throwable {

    Object[] args = pjp.getArgs();
    if (args.length > 0) {
      for (Object arg : args) {
        if (arg instanceof HttpServletRequest) {
          HttpServletRequest request = (HttpServletRequest) arg;
          String correlationId = request.getHeader(CORRELATION_ID);
          if (correlationId != null) {
            MDC.put("correlationId", correlationId);
          }
          break;
        }
      }
    }
    try {
      return pjp.proceed(args);
    } finally {
      MDC.clear();
    }
  }

  @Around("bean(*ModuleImpl)")
  public Object aroundModuleAdvice(ProceedingJoinPoint pjp) throws Throwable {
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    Method method = signature.getMethod();
    Object[] args = pjp.getArgs();

    MethodRequest request = new MethodRequest();
    UUID correlationId =
        MDC.get("correlationId") == null ? null : UUID.fromString(MDC.get("correlationId"));
    request.setCorrelationId(correlationId);
    request.setName(method.getDeclaringClass().getName() + "." + method.getName());

    log.info("request: {}", request.requestToString());

    try {
      Object result = pjp.proceed(args);
      log.info("response: {}", request.responseToString());
      return result;
    } catch (Exception e) {
      request.setException(e);
      int status = 500;
      if (e instanceof BLException) {
        BLException blException = (BLException) e;
        status = blException.getStatus().value();
      }

      if (status == 500) {
        log.error("response: {}", request.errorResponseToString(), e);
      } else {
        log.info("response: {}", request.errorResponseToString());
      }
      throw e;
    }
  }

  @Getter
  @Setter
  static class MethodRequest {

    private UUID correlationId;
    private String name;
    private Exception exception;

    public String requestToString() {
      return "{" +
          "correlationId=" + correlationId +
          ", name='" + name + '\'' +
          '}';
    }

    public String responseToString() {
      return "{" +
          "correlationId=" + correlationId +
          ", name='" + name + '\'' +
          '}';
    }

    public String errorResponseToString() {
      return "{" +
          "correlationId=" + correlationId +
          ", name='" + name + '\'' +
          ", exception=" + exception +
          '}';
    }
  }

}
