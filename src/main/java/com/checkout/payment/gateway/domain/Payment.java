package com.checkout.payment.gateway.domain;

import com.checkout.payment.gateway.domain.commands.SubmitPaymentCommand;
import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
//Comment for review - All args constructor is purely for entity conversions, so can avoid use of reflection
@AllArgsConstructor
public class Payment {

  //Comment for review - We do not pass around the API resources, nor use the persistence entities, we
  // have a specific domain entity for the business logic instead. There is not much here, but
  // would be extended in a more complete implementation

  private String merchant;
  private UUID id;
  private String cardNumber;
  private String cvv;
  private int expiryMonth;
  private int expiryYear;
  private SupportedCurrency currency;
  private int amount;
  private Status status;
  private Instant lastUpdated;
  private String authorisationCode;

  //Comment for review - Although we have validated the inbound API request, if we later add more
  // ways to submit payments, i.e. via a command via a message broker. So should validate
  // the properties here as well, but I have run out of time

  public static Payment createNewPayment(SubmitPaymentCommand command, Instant updateTime) {
    return new Payment(command.merchant(), UUID.randomUUID(), command.cardNumber(), command.cvv(),
        command.expiryMonth(), command.expiryYear(), command.currency(),
        command.amount(), Status.SUBMITTED, updateTime, null);
  }

  public void markPaymentAsAuthorised(String authorisationCode, Instant updateTime) {
    markPaymentAsProcessed(authorisationCode, Status.AUTHORIZED, updateTime);
  }

  public void markPaymentAsDeclined(Instant updateTime) {
    markPaymentAsProcessed(null, Status.DECLINED, updateTime);
  }

  public enum Status {
    SUBMITTED, AUTHORIZED, DECLINED;
  }

  private void markPaymentAsProcessed(String authorisationCode, Status newStatus,
      Instant updateTime) {
    if (this.status != Status.SUBMITTED) {
      throw new IllegalStateException("Can only move from SUBMITTED to " + status);
    }
    this.status = newStatus;
    this.authorisationCode = authorisationCode;
    this.lastUpdated = updateTime;
  }
}
