package com.checkout.payment.gateway.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "acquiring.bank")
@Data
public class AcquiringBankProperties {

  private String host;
}
