package com.checkout.payment.gateway.api.resources.validation;

import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest;
import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest.ValidExpiry;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CardExpiryValidator implements ConstraintValidator<ValidExpiry, SubmitPaymentRequest> {

  private final Clock localClock;

  @Override
  public boolean isValid(SubmitPaymentRequest request, ConstraintValidatorContext context) {
    //Review comment, cards expire at the end of the expiry month, clock is local timezone so
    // we check the date on the card against the time in the relevant region.
    //
    // If the service needs to support multiple timezones, then would need something passing in
    // to indicate the timezone to check against
    YearMonth currentYearMonth = YearMonth.now(localClock);
    try {
      YearMonth expiryYearMonth = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
      return !expiryYearMonth.isBefore(currentYearMonth);
    } catch (NullPointerException | DateTimeException ex) {
      return false;
    }
  }
}
