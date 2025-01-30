package com.checkout.payment.gateway.persistence.inmemory.repository;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.Payment.Status;
import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import java.time.Instant;
import java.util.UUID;

//Comment for review - If using spring-data-jpa/mongo, etc we would most likely need to add
// implementation specific annotations here, we also may want to tweak the types to work
// for the selected db (Timestamps instead of Instant, etc), so add a specific entity here to
// abstract this away from the business domain. Although here it does seem like a waste as we
// are just putting into a map and no changes from the domain object are actually required.
//
// We also we wouldn't be able to use a record if we required a proper Entity class for
// spring-data-jpa/mongo, would need to be a class
public record PaymentEntity(
    String merchant,
    String cardNumber,
    String cvv,
    int expiryMonth,
    int expiryYear,
    SupportedCurrency currency,
    int amount,
    Status status,
    Instant lastUpdated,
    String authorizationCode
) {

  //Comment for review - We can use libraries like MapStruct to avoid this sort of boilerplate,
  // but this is the cost of taking this approach and may not be worth it
  public static PaymentEntity fromDomain(Payment payment) {
    return new PaymentEntity(payment.getMerchant(), payment.getCardNumber(), payment.getCvv(),
        payment.getExpiryMonth(), payment.getExpiryYear(), payment.getCurrency(),
        payment.getAmount(), payment.getStatus(), payment.getLastUpdated(),
        payment.getAuthorisationCode());
  }

  //Comment for review - We can use libraries like MapStruct to avoid this sort of boilerplate,
  // but this is the cost of taking this approach and may not be worth it
  public Payment toDomain(UUID id) {
    return new Payment(merchant, id, cardNumber, cvv, expiryMonth, expiryYear, currency, amount,
        status,
        lastUpdated, authorizationCode);
  }
}