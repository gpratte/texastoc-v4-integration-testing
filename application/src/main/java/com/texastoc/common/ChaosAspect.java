package com.texastoc.common;

import static com.texastoc.common.ChaosAspect.ExceptionType.RUNTIME_EXCEPTION;

import com.texastoc.config.IntegrationTestingConfig;
import com.texastoc.exception.BLException;
import com.texastoc.exception.BLType;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Slf4j
@Component
public class ChaosAspect {

  enum ExceptionType {
    RUNTIME_EXCEPTION,
    DENIED_EXCEPTION
  }

  private static final Random RANDOM = new Random();

  // Repeating chaos will only work if running only one server and one client. This is a hack to
  // cause a synthetic error in New Relic which retries twice when there is an error.
  // https://discuss.newrelic.com/t/api-monitoring-with-retry/55818/2
  // "Synthetics already implements a 3-strike policy for monitor failures. When a monitor
  // encounters its first check failure, it will immediately queue 2 additional checks from the
  // same location which are ran almost immediately. If all 3 of those fail, only then does the
  // monitor enter a “failing” state and any relevant notifications sent."
  private ThrownExceptionInfo thrownExceptionInfo;

  private final IntegrationTestingConfig integrationTestingConfig;

  public ChaosAspect(IntegrationTestingConfig integrationTestingConfig) {
    this.integrationTestingConfig = integrationTestingConfig;
    log.info("integrationTestingConfig={}", integrationTestingConfig);
  }

  @Before("execution(public * com.texastoc.module.*.service..*(..))")
  public void chaos(JoinPoint joinPoint) {
    if (!integrationTestingConfig.isAllowChaos()) {
      return;
    }
    repeatChaos();
    causeChaos();
  }

  private void repeatChaos() {
    // Check if repeating chaos is configured
    if (integrationTestingConfig.getRepeatChaos() > 0) {
      // Check if chaos has been created
      if (thrownExceptionInfo != null) {
        int count = thrownExceptionInfo.getCount();
        if (count < integrationTestingConfig.getRepeatChaos()) {
          // More chaos
          thrownExceptionInfo.setCount(++count);
          if (thrownExceptionInfo.getExceptionType() == RUNTIME_EXCEPTION) {
            throwRuntime();
          } else {
            throwDenied();
          }
        } else {
          // No more repeated chaos
          thrownExceptionInfo = null;
        }
      }
    }
  }

  private void causeChaos() {
    if (RANDOM.nextInt(integrationTestingConfig.getChaosFrequency()) == 0) {
      // Time to cause some chaos. Randomly choose an exception type.
      ExceptionType exceptionType = ExceptionType.values()[RANDOM.nextInt(
          ExceptionType.values().length)];

      if (integrationTestingConfig.getRepeatChaos() > 0) {
        // Repeating chaos is configured to flag this first time
        thrownExceptionInfo = new ThrownExceptionInfo(exceptionType, 1);
      }

      switch (exceptionType) {
        case RUNTIME_EXCEPTION:
          throwRuntime();
        case DENIED_EXCEPTION:
          throwDenied();
        default:
          throw new RuntimeException("should never happen");
      }
    }
  }

  private void throwRuntime() {
    String correlationId = MDC.get("correlationId") == null ? null : MDC.get("correlationId");
    log.info("correlationId=" + correlationId + ", chaos throwing RuntimeException");
    throw new RuntimeException("chaos");
  }

  private void throwDenied() {
    String correlationId = MDC.get("correlationId") == null ? null : MDC.get("correlationId");
    log.info("correlationId=" + correlationId + ", chaos throwing DENIED");
    throw new BLException(BLType.DENIED);
  }

  @AllArgsConstructor
  @Getter
  @Setter
  private static class ThrownExceptionInfo {

    private ExceptionType exceptionType;
    private int count;
  }
}
