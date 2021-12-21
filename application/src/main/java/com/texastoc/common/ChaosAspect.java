package com.texastoc.common;

import com.texastoc.config.IntegrationTestingConfig;
import com.texastoc.exception.BLException;
import com.texastoc.exception.BLType;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Slf4j
@Component
public class ChaosAspect {

  private static final Random RANDOM = new Random();

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
    if (RANDOM.nextInt(integrationTestingConfig.getChaosFrequency()) == 0) {
      log.info("chaos throwing RuntimeException");
      throw new RuntimeException("chaos");
    }
    if (RANDOM.nextInt(integrationTestingConfig.getChaosFrequency()) == 0) {
      log.info("chaos throwing DENIED");
      throw new BLException(BLType.DENIED);
    }
  }
}
