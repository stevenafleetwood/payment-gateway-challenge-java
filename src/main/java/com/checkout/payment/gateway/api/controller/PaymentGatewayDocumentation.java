package com.checkout.payment.gateway.api.controller;

import com.checkout.payment.gateway.api.resources.ErrorResponse;
import com.checkout.payment.gateway.api.resources.PaymentResponse;
import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PaymentGatewayDocumentation {

  @Operation(
      summary = "Submit a payment",
      description = "This endpoint will submit a payment for processing",
      tags = {"payments"},
      //security = {}
      responses = {
          @ApiResponse(responseCode = "200", description = "Payment processed"),
          @ApiResponse(responseCode = "400", description = "Invalid payment details provided",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      }
  )
  @PostMapping("/submitPayment")
  ResponseEntity<PaymentResponse> submitPayment(@Valid @RequestBody SubmitPaymentRequest request,
      BindingResult bindingResult);

  @Operation(
      summary = "Retrieve an existing payment",
      description = "This endpoint will return one of your existing payments",
      tags = {"payments"},
      //security = {}
      responses = {
          @ApiResponse(responseCode = "200", description = "Payment found"),
          @ApiResponse(responseCode = "404", description = "Payment not found",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      }
  )
  @GetMapping("/retrievePayment/{id}")
  ResponseEntity<PaymentResponse> retrievePayment(@Valid @PathVariable UUID id);
}