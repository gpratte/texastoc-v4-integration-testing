package com.texastoc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.exception.BLException;
import java.lang.annotation.Annotation;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Aspect
@Order(1)
@Slf4j
@Component
/**
 * TODO move to an ApiGateway.
 * <p>Logs in request/responses of the controller endpoints</p>
 */
public class LoggingAspect {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Around("bean(*Controller)")
  public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
    UUID correlationId = UUID.randomUUID();
    MDC.put("correlationId", correlationId.toString());

    ApiRequest request = new ApiRequest();
    request.setCorrelationId(correlationId);

    MethodSignature signature = (MethodSignature) pjp.getSignature();
    Method method = signature.getMethod();
    for (Annotation annotation : method.getDeclaredAnnotations()) {
      if (annotation.annotationType() == GetMapping.class) {
        request.setAction("GET");
      } else if (annotation.annotationType() == PostMapping.class) {
        request.setAction("POST");
      } else if (annotation.annotationType() == PutMapping.class) {
        request.setAction("PUT");
      } else if (annotation.annotationType() == DeleteMapping.class) {
        request.setAction("DELETE");
      }
    }

    Object[] args = pjp.getArgs();
    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof HttpServletRequest) {
          HttpServletRequest httpServletRequest = (HttpServletRequest) args[i];
          request.setUri(httpServletRequest.getRequestURI());
          request.setContentType(httpServletRequest.getContentType());
          break;
        }
      }
    }

    log.info("request: {}", request);

    try {
      Object result = pjp.proceed(args);

      ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);
      if (responseStatus != null) {
        request.setStatus(responseStatus.value().value());
      }
      log.info("response: {}", request);
      return result;
    } catch (Exception e) {
      if (e instanceof BLException) {
        BLException blException = (BLException) e;
        request.setStatus(blException.getStatus().value());
        request.setBlException(blException);
      } else {
        request.setStatus(500);
      }

      if (request.getStatus() == 500) {
        log.error("response: {}", request, e);
      } else {
        log.info("response: {}", request);
      }
      throw e;
    } finally {
      MDC.clear();
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

    @Override
    public String toString() {
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
}
