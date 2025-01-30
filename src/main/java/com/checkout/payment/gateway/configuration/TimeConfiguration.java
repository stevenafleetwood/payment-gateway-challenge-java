package com.checkout.payment.gateway.configuration;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class TimeConfiguration {

  private static final ZoneId UTC = ZoneId.of("UTC");

  //Comment for review - This should be parameterised really if we want to supply the local timezone
  // if need to support multiple local timezone then would need providing in the requests or somethin
  // as opposed to fixing like this
  private static final ZoneId LOCAL_TIMEZONE = ZoneId.of("Europe/London");

  //Comment for review - We have clock here to make testing easier, so we can 'fix'
  // time for the tests
  @Bean
  public Clock utcClock() {
    return Clock.system(UTC);
  }

  @Bean
  Clock localClock() {
    return Clock.system(LOCAL_TIMEZONE);
  }

}
