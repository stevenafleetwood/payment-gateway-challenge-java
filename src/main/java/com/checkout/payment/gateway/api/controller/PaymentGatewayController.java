package com.checkout.payment.gateway.api.controller;

import com.checkout.payment.gateway.api.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.api.exception.PaymentRejectedException;
import com.checkout.payment.gateway.api.resources.PaymentResponse;
import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest;
import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.commands.SubmitPaymentCommand;
import com.checkout.payment.gateway.service.PaymentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
@RequiredArgsConstructor
public class PaymentGatewayController implements PaymentGatewayDocumentation {

  private final PaymentService paymentService;

  //Review comment - The requirements describe an RPC API as opposed to a Restful CRUD API

  //Review comment - I considered investigating executing the submitPaymentCommand asynchronously,
  // as API calls out to the Acquiring Bank will be slow, but as there are no non-functional
  // requirements around concurrent requests, etc, decided to keep it simple, as would need to test
  // if it even helped or is required

  public ResponseEntity<PaymentResponse> submitPayment(
      @Valid @RequestBody SubmitPaymentRequest request, BindingResult bindingResult) {
    if (!bindingResult.getAllErrors().isEmpty()) {
      throw new PaymentRejectedException(bindingResult.getAllErrors());
    }

    Payment payment = paymentService.submitPayment(new SubmitPaymentCommand(
        getMerchantId(), request.getCardNumber(), request.getCvv(), request.getExpiryMonth(),
        request.getExpiryYear(), request.getCurrency().toDomain(), request.getAmount())
    );
    return ResponseEntity.ok(PaymentResponse.fromDomain(payment));
  }

  public ResponseEntity<PaymentResponse> retrievePayment(@Valid @PathVariable UUID id) {
    return paymentService.getProcessedPayment(getMerchantId(), id)
        .map(payment -> ResponseEntity.ok(PaymentResponse.fromDomain(payment)))
        .orElseThrow(() -> new PaymentNotFoundException(id));
  }

  public String getMerchantId() {
    //Review comment - The service should/could be authenticated, maybe via a JWT, where the sub claim could be
    // the merchant id, and extracted from the SecurityContextHolder. I ran out of time to implement
    // this and is probably out of scope anyway so will just fix the merchant Id for the purpose of
    // this exercise

    //return SecurityContextHolder.getContext().getAuthentication().getName();
    return "merchant_id";
  }
}
