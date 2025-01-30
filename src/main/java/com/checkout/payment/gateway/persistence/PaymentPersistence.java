package com.checkout.payment.gateway.persistence;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.persistence.inmemory.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

//Comment for review - Implement a Facade here, so we are not directly exposing the DB
// implementation directly, i.e. if use spring-data-jpa/mongo the JpaRepository, MongoRepository,
// etc may offer methods that we don't want exposing the to the business logic (deleteById for
// example), also we were to switch to a persistence implementation that offers a totally different
// repository we can insulate the change here.
@Component
@AllArgsConstructor
public class PaymentPersistence {

  private final PaymentRepository repository;

  public void save(Payment payment) {
    repository.save(payment);
  }

  public Optional<Payment> getPayment(UUID id) {
    return repository.getPayment(id);
  }

  //Comment for review - Get processed payment for the specified merchant, so we can limit access
  // to the requesting merchant only, may be out of scope
  public Optional<Payment> getProcessedPayment(String merchant, UUID id) {
    return repository.getProcessedPayment(merchant, id);
  }

}
