package com.checkout.payment.gateway.payment.processing.exception;

public class PaymentProcessingException extends RuntimeException {

  public PaymentProcessingException(String message) {
    super(message);
  }

  public PaymentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
