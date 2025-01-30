package com.checkout.payment.gateway.payment.processing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_AMOUNT;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CURRENCY;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CVV;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_DATE;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_NUMBER;
import static com.checkout.payment.gateway.TestConstants.OBJECT_MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AcquiringBankClientTest {

    @Test
    public void paymentRequestSerialisation() throws JsonProcessingException, JSONException {
        var json = new JSONObject(OBJECT_MAPPER.writeValueAsString(new AcquiringBankClient.PaymentRequest(
            AUTHORISED_CARD_NUMBER, AUTHORISED_CARD_EXPIRY_DATE, AUTHORISED_CARD_CURRENCY, AUTHORISED_CARD_AMOUNT, AUTHORISED_CARD_CVV
        )));

        assertEquals(AUTHORISED_CARD_NUMBER, json.getString("card_number"));
        assertEquals(AUTHORISED_CARD_EXPIRY_DATE, json.getString("expiry_date"));
        assertEquals(AUTHORISED_CARD_CURRENCY, json.getString("currency"));
        assertEquals(AUTHORISED_CARD_AMOUNT, json.getInt("amount"));
        assertEquals(AUTHORISED_CARD_CVV, json.getString("cvv"));
    }


}