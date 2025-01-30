package com.checkout.payment.gateway.api.resources.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest;
import config.TestTimeConfiguration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CardExpiryValidatorTest {

  public CardExpiryValidator validator =
      new CardExpiryValidator(TestTimeConfiguration.getClock(TestTimeConfiguration.LOCAL_ZONE_ID));

  public static Stream<Arguments> badInputs() {
    return Stream.of(
        Arguments.of(null, 2025),
        Arguments.of(12, null),
        Arguments.of(-1, 2025),
        Arguments.of(12, -1)
    );
  }

  @ParameterizedTest
  @MethodSource("badInputs")
  void testHandlesBadInputs(Integer expiryMonth, Integer expiryYear) {
    assertFalse(validator.isValid(
        SubmitPaymentRequest.builder()
            .expiryMonth(null)
            .expiryYear(null)
            .build(),
        null
    ));
  }

  @Test
  void testMonthBeforeCurrentDate() {
    assertFalse(validator.isValid(
        SubmitPaymentRequest.builder()
            .expiryMonth(TestTimeConfiguration.FIXED_MONTH - 1)
            .expiryYear(TestTimeConfiguration.FIXED_YEAR)
            .build(),
        null
    ));
  }

  @Test
  void testCurrentMonth() {
    assertTrue(validator.isValid(
        SubmitPaymentRequest.builder()
            .expiryMonth(TestTimeConfiguration.FIXED_MONTH)
            .expiryYear(TestTimeConfiguration.FIXED_YEAR)
            .build(),
        null
    ));
  }

  @Test
  void testNextMonth() {
    assertTrue(validator.isValid(
        SubmitPaymentRequest.builder()
            .expiryMonth(TestTimeConfiguration.FIXED_MONTH + 1)
            .expiryYear(TestTimeConfiguration.FIXED_YEAR)
            .build(),
        null
    ));
  }

  @Test
  void testPreviousMonthNextYear() {
    assertTrue(validator.isValid(
        SubmitPaymentRequest.builder()
            .expiryMonth(TestTimeConfiguration.FIXED_MONTH - 1)
            .expiryYear(TestTimeConfiguration.FIXED_YEAR + 1)
            .build(),
        null
    ));
  }

}