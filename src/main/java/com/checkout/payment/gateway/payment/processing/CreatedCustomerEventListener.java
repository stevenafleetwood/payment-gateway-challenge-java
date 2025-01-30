package com.checkout.payment.gateway.payment.processing;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import com.checkout.payment.gateway.domain.events.PaymentCreatedEvent;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.payment.processing.client.AcquiringBankClient;
import com.checkout.payment.gateway.payment.processing.client.AcquiringBankClient.PaymentRequest;
import com.checkout.payment.gateway.payment.processing.client.AcquiringBankClient.PaymentResponse;
import com.checkout.payment.gateway.payment.processing.exception.PaymentProcessingException;
import com.checkout.payment.gateway.persistence.PaymentPersistence;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatedCustomerEventListener {

  private static final String EXPIRY_DATE_FMT = "0%d/%d";

  private final AcquiringBankClient client;
  private final PaymentPersistence paymentPersistence;
  private final Clock utcClock;

  //Review comment, if we were using proper transactions, as opposed to the in memory persistence,
  // then would want to make sure this listener operates in its own transaction, so as not to
  // rollback the publishing transaction that initially creates the payment if handles this event
  // fails
  //
  // Also as we are wanting synchronous execution for our event, no @Async or anything here
  @EventListener
  public void handle(PaymentCreatedEvent paymentCreatedEvent) {
    log.debug("Handling PaymentCreatedEvent: {}", paymentCreatedEvent);
    paymentPersistence.getPayment(paymentCreatedEvent.id()).ifPresentOrElse(
        this::processPayment,
        () -> {
          throw new EventProcessingException(
              "Cannot process payment, as not in DB " + paymentCreatedEvent);
        }
    );
  }

  private void processPayment(Payment payment) {
    try {
      PaymentResponse response = client.authorisePayment(new PaymentRequest(
          payment.getCardNumber(),
          String.format(EXPIRY_DATE_FMT, payment.getExpiryMonth(), payment.getExpiryYear()),
          mapCurrency(payment.getCurrency()),
          payment.getAmount(),
          payment.getCvv()
      ));
      if (response.getAuthorized()) {
        log.debug("Payment {} authorised with code {}", payment.getId(),
            response.getAuthorizationCode());
        //Review comment - We persist the auth code, as I assume it is useful for auditing, etc
        payment.markPaymentAsAuthorised(response.getAuthorizationCode(), Instant.now(utcClock));
      } else {
        log.debug("Payment {} declined}", payment.getId());
        payment.markPaymentAsDeclined(Instant.now(utcClock));
      }
    } catch (PaymentProcessingException ex) {
      throw new EventProcessingException("Cannot process payment", ex);
    }

    paymentPersistence.save(payment);
  }

  private String mapCurrency(SupportedCurrency currency) {
    return switch (currency) {
      case EUR -> "EUR";
      case GBP -> "GBP";
      case USD -> "USD";
    };
  }

}
