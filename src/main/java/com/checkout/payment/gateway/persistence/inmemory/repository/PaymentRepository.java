package com.checkout.payment.gateway.persistence.inmemory.repository;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.Payment.Status;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

  private final HashMap<UUID, PaymentEntity> payments = new HashMap<>();

  public void save(Payment payment) {
    payments.put(
        payment.getId(),
        PaymentEntity.fromDomain(payment)
    );
  }

  public Optional<Payment> getPayment(UUID id) {
    return Optional.ofNullable(payments.get(id)).map(entity -> entity.toDomain(id));
  }

  public Optional<Payment> getProcessedPayment(String merchant, UUID id) {
    return Optional.ofNullable(payments.get(id))
        .filter(
            payment -> payment.merchant().equals(merchant) && payment.status() != Status.SUBMITTED)
        .map(entity -> entity.toDomain(id));
  }

}
