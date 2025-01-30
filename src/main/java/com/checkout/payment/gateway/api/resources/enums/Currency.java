package com.checkout.payment.gateway.api.resources.enums;

import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;


//Comment for review - Changing enums in an API is a breaking change, so change schema to a string
// and explain the intent, not sure if this is actually a good idea or not?
@Schema(
    type = "String",
    example = "USD",
    description = """
        The currency of the payment, possible values are:
        * 'USD'
        * 'EUR'
        * 'GBP'
        
        These values may be extended within this version of the API.""")
public enum Currency {
  USD, EUR, GBP;

  public static Currency fromDomain(SupportedCurrency domain) {
    return switch (domain) {
      case USD -> USD;
      case EUR -> EUR;
      case GBP -> GBP;
    };
  }

  public SupportedCurrency toDomain() {
    return switch (this) {
      case USD -> SupportedCurrency.USD;
      case EUR -> SupportedCurrency.EUR;
      case GBP -> SupportedCurrency.GBP;
    };
  }
}
