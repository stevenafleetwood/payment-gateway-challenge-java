package com.checkout.payment.gateway.api.resources;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest.ValidExpiry;
import com.checkout.payment.gateway.api.resources.enums.Currency;
import com.checkout.payment.gateway.api.resources.validation.CardExpiryValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@ValidExpiry
public class SubmitPaymentRequest implements Serializable {

  @Schema(
      description = "Credit card number between 14 and 19 numerical characters",
      example = "12345678901234"
  )
  @JsonProperty("card_number")
  @Pattern(regexp = "[\\d]{14,19}", message = "{validation.payment_submission.card.number.invalid}")
  @NotBlank(message = "{validation.payment_submission.card.number.invalid}")
  private String cardNumber;

  @Schema(
      description = "Credit card expiry month",
      example = "6"
  )
  @JsonProperty("expiry_month")
  @Min(value = 1, message = "{validation.payment_submission.card.expiry.invalid}")
  @Max(value = 12, message = "{validation.payment_submission.card.expiry.invalid}")
  @NotNull(message = "{validation.payment_submission.card.expiry.invalid}")
  private Integer expiryMonth;

  @Schema(
      description = "Credit card expiry year",
      example = "2027"
  )
  @NotNull(message = "{validation.payment_submission.card.expiry.invalid}")
  @JsonProperty("expiry_year")
  private Integer expiryYear;

  @NotNull(message = "{validation.payment_submission.currency.invalid}")
  private Currency currency;

  @Schema(
      description = "Payment amount in the minor currency unit",
      example = "600"
  )
  @NotNull(message = "{validation.payment_submission.amount.invalid}")
  @Min(value = 0, message = "{validation.payment_submission.amount.invalid}")
  private Integer amount;

  @Schema(
      description = "Credit card verification value between 3 and 4 numerical characters",
      example = "123"
  )
  @Pattern(regexp = "[\\d]{3,4}", message = "{validation.payment_submission.cvv.invalid_length}")
  @NotBlank(message = "{validation.payment_submission.cvv.invalid}")
  private String cvv;

  @Documented
  @Target(TYPE)
  @Retention(RUNTIME)
  @Constraint(validatedBy = {CardExpiryValidator.class})
  public @interface ValidExpiry {

    String message() default "{validation.payment_submission.card.expiry.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
  }


}
