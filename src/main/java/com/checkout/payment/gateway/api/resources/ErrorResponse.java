package com.checkout.payment.gateway.api.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  @Schema(
      description = "The details of what caused the error",
      example = "Message describing the cause of the error"
  )
  private String message;
}
