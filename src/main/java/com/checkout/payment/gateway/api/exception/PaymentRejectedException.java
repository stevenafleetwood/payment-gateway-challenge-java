package com.checkout.payment.gateway.api.exception;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.validation.ObjectError;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@RequiredArgsConstructor
public class PaymentRejectedException extends RuntimeException {

  private final List<ObjectError> errors;
}
