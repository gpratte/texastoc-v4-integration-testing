package com.texastoc.common;

import com.texastoc.config.IntegrationTestingConfig;
import com.texastoc.exception.PermissionDeniedException;
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
  }

  @Before("bean(*Controller)")
  public void chaos(JoinPoint joinPoint) {
    if (!integrationTestingConfig.isAllowChaos()) {
      return;
    }
    if (RANDOM.nextInt(integrationTestingConfig.getChaosFrequency()) == 0) {
      log.info("controller throwing RuntimeException");
      throw new RuntimeException("chaos");
    }
    if (RANDOM.nextInt(integrationTestingConfig.getChaosFrequency()) == 0) {
      log.info("controller throwing PermissionDeniedException");
      throw new PermissionDeniedException("chaos");
    }
  }
}
