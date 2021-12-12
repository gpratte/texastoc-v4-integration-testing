package com.texastoc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Method;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

@Aspect
@Order(1)
@Slf4j
@Component
public class LoggingAspect {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Around("bean(*Controller)")
  public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
    UUID correlationId = UUID.randomUUID();
    MDC.put("correlationId", correlationId.toString());
    ObjectNode request = OBJECT_MAPPER.createObjectNode();

    ObjectNode data = OBJECT_MAPPER.createObjectNode();
    data.put("correlationId", correlationId.toString());
    Object[] args = pjp.getArgs();
    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof HttpServletRequest) {
          HttpServletRequest httpServletRequest = (HttpServletRequest) args[i];
          data.put("uri", httpServletRequest.getRequestURI());
          break;
        }
      }
    }

    request.set("request", data);
    log.info(OBJECT_MAPPER.writeValueAsString(request));

    Object result = null;
    result = pjp.proceed(args);

    ObjectNode response = OBJECT_MAPPER.createObjectNode();
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    Method method = signature.getMethod();
    ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);
    data.put("status", responseStatus.value().value());
    response.set("response", data);
    log.info(OBJECT_MAPPER.writeValueAsString(response));
    MDC.clear();
    return result;
  }
}
