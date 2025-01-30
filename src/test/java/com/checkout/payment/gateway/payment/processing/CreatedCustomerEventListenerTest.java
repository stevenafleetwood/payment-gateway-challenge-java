package com.checkout.payment.gateway.payment.processing;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import com.checkout.payment.gateway.domain.events.PaymentCreatedEvent;
import com.checkout.payment.gateway.payment.processing.client.AcquiringBankClient;
import com.checkout.payment.gateway.persistence.PaymentPersistence;
import config.TestTimeConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_AMOUNT;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_AUTH_CODE;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CURRENCY;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CVV;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_DATE;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_MONTH;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_YEAR;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatedCustomerEventListenerTest {

    @Mock
    public AcquiringBankClient client;

    @Mock
    public PaymentPersistence persistence;

    private Clock fixedClock = TestTimeConfiguration.getClock(TestTimeConfiguration.UTC_ZONE_ID);

    public CreatedCustomerEventListener createdCustomerEventListener;

    @BeforeEach
    public void setup() {
        createdCustomerEventListener = new CreatedCustomerEventListener(client, persistence, fixedClock);
    }

    public static Stream<Arguments> scenarios() {
        return Stream.of(
            Arguments.of(true, Payment.Status.AUTHORIZED, AUTHORISED_CARD_AUTH_CODE),
            Arguments.of(false, Payment.Status.DECLINED, null)
        );
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    public void testEventFlow(boolean isAuthorised, Payment.Status expStatus, String expAuthorisationCode) {
        UUID id = UUID.randomUUID();
        when(persistence.getPayment(id)).thenReturn(Optional.of(buildPayment(id)));
        when(client.authorisePayment(new AcquiringBankClient.PaymentRequest(
            AUTHORISED_CARD_NUMBER, AUTHORISED_CARD_EXPIRY_DATE, AUTHORISED_CARD_CURRENCY, AUTHORISED_CARD_AMOUNT, AUTHORISED_CARD_CVV)
        )).thenReturn(
            new AcquiringBankClient.PaymentResponse(isAuthorised, expAuthorisationCode)
        );

        createdCustomerEventListener.handle(new PaymentCreatedEvent(id));

        ArgumentCaptor<Payment> updatedPayment = ArgumentCaptor.forClass(Payment.class);
        verify(persistence).save(updatedPayment.capture());
        assertEquals(new Payment("",
            id,
            AUTHORISED_CARD_NUMBER,
            AUTHORISED_CARD_CVV,
            AUTHORISED_CARD_EXPIRY_MONTH,
            AUTHORISED_CARD_EXPIRY_YEAR,
            SupportedCurrency.valueOf(AUTHORISED_CARD_CURRENCY),
            AUTHORISED_CARD_AMOUNT,
            expStatus,
            Instant.now(fixedClock),
            expAuthorisationCode
        ), updatedPayment.getValue());
    }

    @Test
    public void testErrorWithClientApiCall() {
        UUID id = UUID.randomUUID();

        when(persistence.getPayment(id)).thenReturn(Optional.of(buildPayment(id)));
        when(client.authorisePayment(any())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> createdCustomerEventListener.handle(new PaymentCreatedEvent(id)));
        verify(persistence, times(0)).save(any());
    }

    private static Payment buildPayment(UUID id) {
        return new Payment("",
            id,
            AUTHORISED_CARD_NUMBER,
            AUTHORISED_CARD_CVV,
            AUTHORISED_CARD_EXPIRY_MONTH,
            AUTHORISED_CARD_EXPIRY_YEAR,
            SupportedCurrency.valueOf(AUTHORISED_CARD_CURRENCY),
            AUTHORISED_CARD_AMOUNT,
            Payment.Status.SUBMITTED,
            Instant.now(),
            null
        );
    }
}