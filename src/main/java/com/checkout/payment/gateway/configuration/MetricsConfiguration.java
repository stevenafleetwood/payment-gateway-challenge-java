package com.checkout.payment.gateway.configuration;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

  //Review comment - Timed metrics for external APIs can be useful in case of latency issues
  @Bean
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
