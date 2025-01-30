package com.checkout.payment.gateway.api.exception;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class PaymentNotFoundException extends RuntimeException {

  private final UUID id;

  public PaymentNotFoundException(UUID id) {
    this.id = id;
  }
}
