package com.checkout.payment.gateway.domain;

import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_AMOUNT;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CURRENCY;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CVV;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_MONTH;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_YEAR;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.checkout.payment.gateway.domain.Payment.Status;
import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PaymentTest {

  private static Stream<Arguments> scenarios() {
    return Stream.of(
        Arguments.of(Status.AUTHORIZED),
        Arguments.of(Status.DECLINED)
    );
  }

  @ParameterizedTest
  @MethodSource("scenarios")
  public void testMarkPaymentAsDeclined_CannotTransitionFromInvalidState(Status currentStatus) {
    Payment payment = new Payment("",
        UUID.randomUUID(),
        AUTHORISED_CARD_NUMBER,
        AUTHORISED_CARD_CVV,
        AUTHORISED_CARD_EXPIRY_MONTH,
        AUTHORISED_CARD_EXPIRY_YEAR,
        SupportedCurrency.valueOf(AUTHORISED_CARD_CURRENCY),
        AUTHORISED_CARD_AMOUNT,
        currentStatus,
        Instant.now(),
        null
    );

    assertThrows(IllegalStateException.class, () ->
        payment.markPaymentAsDeclined(Instant.now()));
  }

  @ParameterizedTest
  @MethodSource("scenarios")
  public void testMarkPaymentAsAuthorised_CannotTransitionFromInvalidState(Status currentStatus) {
    Payment payment = new Payment("",
        UUID.randomUUID(),
        AUTHORISED_CARD_NUMBER,
        AUTHORISED_CARD_CVV,
        AUTHORISED_CARD_EXPIRY_MONTH,
        AUTHORISED_CARD_EXPIRY_YEAR,
        SupportedCurrency.valueOf(AUTHORISED_CARD_CURRENCY),
        AUTHORISED_CARD_AMOUNT,
        currentStatus,
        Instant.now(),
        null
    );

    assertThrows(IllegalStateException.class, () ->
        payment.markPaymentAsAuthorised("", Instant.now()));
  }

}