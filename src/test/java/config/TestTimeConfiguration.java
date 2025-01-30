package config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestTimeConfiguration {

  //Ensure fixed time is before the earliest valid card expiry in the imposter
  public static final Integer FIXED_YEAR = 2025;
  public static final Integer FIXED_MONTH = 2;
  public static final Instant FIXED_TIME = Instant.parse(FIXED_YEAR + "-0" + FIXED_MONTH + "-01T12:00:00Z");
  public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
  public static final ZoneId LOCAL_ZONE_ID = ZoneId.of("Europe/London");

  @Bean("utcClock")
  public Clock utcClock() {
    return getClock(UTC_ZONE_ID);
  }

  @Bean("localClock")
  Clock localClock() {
    return getClock(LOCAL_ZONE_ID);
  }

  public static Clock getClock(ZoneId zoneId) {
    return Clock.fixed(FIXED_TIME, zoneId);
  }

}
