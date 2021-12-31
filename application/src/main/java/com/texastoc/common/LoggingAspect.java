package com.texastoc.common;

import com.texastoc.exception.BLException;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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

  @Around("bean(*ModuleImpl)")
  public Object aroundModuleAdvice(ProceedingJoinPoint pjp) throws Throwable {
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    Method method = signature.getMethod();
    Object[] args = pjp.getArgs();

    MethodRequest request = new MethodRequest();
    request.setName(method.getDeclaringClass().getName() + "." + method.getName());

    log.info("request={}", request.requestToString());

    try {
      Object result = pjp.proceed(args);
      log.info("response={}", request.responseToString());
      return result;
    } catch (Exception e) {
      request.setException(e);
      int status = 500;
      if (e instanceof BLException) {
        BLException blException = (BLException) e;
        status = blException.getStatus().value();
      }

      if (status == 500) {
        log.error("response={}", request.errorResponseToString(), e);
      } else {
        log.info("response={}", request.errorResponseToString());
      }
      throw e;
    }
  }

  @Getter
  @Setter
  static class MethodRequest {

    private String name;
    private Exception exception;

    public String requestToString() {
      return "{name=" + name + "}";
    }

    public String responseToString() {
      return "{name=" + name + "}";
    }

    public String errorResponseToString() {
      return "{name=" + name + " exception=" + exception + "}";
    }
  }
}
