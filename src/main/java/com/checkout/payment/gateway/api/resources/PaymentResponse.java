package com.checkout.payment.gateway.api.resources;

import com.checkout.payment.gateway.api.resources.enums.Currency;
import com.checkout.payment.gateway.api.resources.enums.PaymentStatus;
import com.checkout.payment.gateway.domain.Payment;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  @Schema(
      description = "The unique id for this payment",
      example = "4a0d68e4-a7e6-49f0-ac25-838e4794c429"
  )
  private UUID id;

  private PaymentStatus status;

  @Schema(
      description = "Credit card number between 14 and 19 numerical characters",
      example = "12345678901234"
  )
  @JsonSetter("card_number")
  private String cardNumber;

  @Schema(
      description = "Credit card expiry month",
      example = "6"
  )
  @JsonSetter("expiry_month")
  private Integer expiryMonth;

  @Schema(
      description = "Credit card expiry year",
      example = "2027"
  )
  @JsonSetter("expiry_year")
  private Integer expiryYear;

  private Currency currency;

  @Schema(
      description = "Payment amount in the minor currency unit",
      example = "600"
  )
  private Integer amount;

  public static PaymentResponse fromDomain(Payment payment) {
    return new PaymentResponse(payment.getId(), mapDomainStatus(payment.getStatus()),
        maskCardNumber(payment.getCardNumber()), payment.getExpiryMonth(), payment.getExpiryYear(),
        Currency.fromDomain(payment.getCurrency()), payment.getAmount());
  }

  private static PaymentStatus mapDomainStatus(Payment.Status domainStatus) {
    return switch (domainStatus) {
      case DECLINED -> PaymentStatus.DECLINED;
      case AUTHORIZED -> PaymentStatus.AUTHORIZED;
      case SUBMITTED -> throw new IllegalStateException(
          "Payments with submitted state should not be return to merchants"); //TODO we should error here as not valid status to return to in API
    };
  }

  private static String maskCardNumber(String cardNumber) {
    //TODO maybe we should guard against card numbers less than 4 characters?
    int beginIndex = cardNumber.length() - 4;
    return cardNumber.substring(beginIndex);
  }
}
