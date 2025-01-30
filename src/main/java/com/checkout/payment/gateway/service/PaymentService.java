package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.commands.SubmitPaymentCommand;
import com.checkout.payment.gateway.domain.events.PaymentCreatedEvent;
import com.checkout.payment.gateway.persistence.PaymentPersistence;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final ApplicationEventPublisher eventPublisher;
  private final PaymentPersistence paymentPersistence;
  private final Clock utcClock;

  //Review comment - Here I would maybe like to just write the command directly to a message bus,
  // so if this service dies mid-process the command could be tried, etc when another instance picks
  // up the event. However, this would need some kind of polling approach from the merchant client,
  // as opposed to the synchronous behaviour described in the requirement.

  public Payment submitPayment(SubmitPaymentCommand command) {
    //Review comment - Assumption: We store the payment into the DB, which can hopefully offers strong
    // consistency, before calling the Aquiring Bank API. So we can handle any dropped requests to the
    // Aquiring Bank, which may have actually been processed by them, without us losing all record
    // of the payment.
    //
    // For example if the instance of this service making the call dies while the request is in flight
    // or the connection to the Aquiring Bank API times out.
    //
    // We could then have a scheduled task, to look for any old payments still at SUBMITTED state
    // and perform some remediation, this may not be required.
    Payment payment = Payment.createNewPayment(command, Instant.now(utcClock));
    log.debug("Submitting payment {}", payment);
    paymentPersistence.save(payment);

    //Review comment - Observer pattern, so we can extend required actions later without having to
    // change this class
    eventPublisher.publishEvent(new PaymentCreatedEvent(payment.getId()));

    //This is not very good, having to hit the DB again to get upto date state after events have
    // run, so we can synchronously return the response to the client. It might just be better
    // we perform this in a single method here vs trying to implement observer patter for this usecase
    return paymentPersistence.getPayment(payment.getId()).orElseThrow(() ->
        new IllegalStateException("Payment not present in the DB"));
  }

  //Comment for review - The merchant is passed, as the service might be used by multiple merchants,
  //  if not this is not the case it can be removed and just retrieve payments by id
  public Optional<Payment> getProcessedPayment(String merchant, UUID id) {
    log.debug("Requesting access to to payment with ID {}", id);
    return paymentPersistence.getProcessedPayment(merchant, id);
  }
}
