package com.checkout.payment.gateway.api.resources.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "The status of the payment:\n"
        + "Authorized - the payment was authorized by the call to the acquiring bank\n"
        + "Declined - the payment was declined by the call to the acquiring bank",
    example = "Authorized"
)
public enum PaymentStatus {
  AUTHORIZED("Authorized"),
  DECLINED("Declined");

  private final String name;

  PaymentStatus(String name) {
    this.name = name;
  }

  @JsonValue
  public String getName() {
    return this.name;
  }
}
