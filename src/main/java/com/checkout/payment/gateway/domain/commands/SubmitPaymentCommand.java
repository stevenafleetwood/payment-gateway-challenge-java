package com.checkout.payment.gateway.domain.commands;

import com.checkout.payment.gateway.domain.enums.SupportedCurrency;

public record SubmitPaymentCommand(String merchant, String cardNumber, String cvv, int expiryMonth,
                                   int expiryYear, SupportedCurrency currency, int amount) {

}
