package com.texastoc.common;

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
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * TODO move to an Api gateway.
 * <p>Logs in request/responses of the controller endpoints</p>
 */
@Aspect
@Order(1)
@Slf4j
@Component
public class LoggingAspect {

  @Around("bean(*Controller)")
  public Object aroundControllerAdvice(ProceedingJoinPoint pjp) throws Throwable {
    UUID correlationId = UUID.randomUUID();
    MDC.put("correlationId", correlationId.toString());

    ApiRequest request = new ApiRequest();
    request.setCorrelationId(correlationId);

    Object[] args = pjp.getArgs();
    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof HttpServletRequest) {
          HttpServletRequest httpServletRequest = (HttpServletRequest) args[i];
          request.setUri(httpServletRequest.getRequestURI());
          request.setContentType(httpServletRequest.getContentType());
          request.setAction(httpServletRequest.getMethod());
          break;
        }
      }
    }

    log.info("request: {}", request.requestToString());

    try {
      Object result = pjp.proceed(args);

      MethodSignature signature = (MethodSignature) pjp.getSignature();
      Method method = signature.getMethod();
      ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);
      if (responseStatus != null) {
        request.setStatus(responseStatus.value().value());
      }
      log.info("response: {}", request.responseToString());
      return result;
    } catch (Exception e) {
      if (e instanceof BLException) {
        BLException blException = (BLException) e;
        blException.setCorrelationId(correlationId);
        request.setStatus(blException.getStatus().value());
        request.setBlException(blException);
      } else {
        request.setStatus(500);
      }

      if (request.getStatus() == 500) {
        log.error("response: {}", request.errorResponseToString(), e);
      } else {
        log.info("response: {}", request.errorResponseToString());
      }
      throw e;
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
  static class ApiRequest {

    private UUID correlationId;
    private String action;
    private String uri;
    private String contentType;
    private Integer status;
    private BLException blException;

    public String requestToString() {
      return "{" +
          "correlationId=" + correlationId +
          ", action='" + action + '\'' +
          ", uri='" + uri + '\'' +
          ", contentType='" + contentType + '\'' +
          '}';
    }

    public String responseToString() {
      return "{" +
          "correlationId=" + correlationId +
          ", action='" + action + '\'' +
          ", uri='" + uri + '\'' +
          ", contentType='" + contentType + '\'' +
          ", status=" + status +
          '}';
    }

    public String errorResponseToString() {
      return "{" +
          "correlationId=" + correlationId +
          ", action='" + action + '\'' +
          ", uri='" + uri + '\'' +
          ", contentType='" + contentType + '\'' +
          ", status=" + status +
          ", blException=" + blException +
          '}';
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
