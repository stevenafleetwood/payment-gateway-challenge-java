package com.checkout.payment.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestConstants {

  private TestConstants() {
  }

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final String AUTHORISED_CARD_NUMBER = "2222405343248877";
  public static final String AUTHORISED_CARD_LAST_4_NUMBERS = "8877";
  public static final String AUTHORISED_CARD_EXPIRY_DATE = "04/2025";
  public static final int AUTHORISED_CARD_EXPIRY_MONTH = 4;
  public static final int AUTHORISED_CARD_EXPIRY_YEAR = 2025;
  public static final int AUTHORISED_CARD_AMOUNT = 100;
  public static final String AUTHORISED_CARD_CVV = "123";
  public static final String AUTHORISED_CARD_CURRENCY = "GBP";
  public static final String AUTHORISED_CARD_AUTH_CODE = "0bb07405-6d44-4b50-a14f-7ae0beff13ad";

  public static final String DECLINED_CARD_NUMBER = "2222405343248112";
  public static final String DECLINED_CARD_LAST_4_NUMBERS = "8112";
  public static final String DECLINED_CARD_EXPIRY_DATE = "01/2026";
  public static final int DECLINED_CARD_EXPIRY_MONTH = 1;
  public static final int DECLINED_CARD_EXPIRY_YEAR = 2026;
  public static final int DECLINED_CARD_AMOUNT = 60000;
  public static final String DECLINED_CARD_CVV = "456";
  public static final String DECLINED_CARD_CURRENCY = "USD";


}
