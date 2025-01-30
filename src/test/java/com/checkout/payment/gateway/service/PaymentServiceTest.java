package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.domain.commands.SubmitPaymentCommand;
import com.checkout.payment.gateway.domain.enums.SupportedCurrency;
import com.checkout.payment.gateway.domain.events.PaymentCreatedEvent;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.persistence.PaymentPersistence;
import config.TestTimeConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_AMOUNT;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CURRENCY;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CVV;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_MONTH;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_YEAR;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentPersistence paymentPersistence;

    private final Clock utcClock = TestTimeConfiguration.getClock(TestTimeConfiguration.UTC_ZONE_ID);

    private PaymentService service;

    @BeforeEach
    public void setup() {
        service = new PaymentService(eventPublisher, paymentPersistence, utcClock);
    }

    @Test
    public void submitPayment_DoesNotCompleteIfPublishedEventsError() {
        doThrow(new EventProcessingException("")).when(eventPublisher).publishEvent(any(PaymentCreatedEvent.class));
        assertThrows(EventProcessingException.class, () -> service.submitPayment(buildCommand()));
    }

    @Test
    public void submitPayment_ReturnsPersistedPayment() {
        Payment expPayment = buildPayment();
        when(paymentPersistence.getPayment(any())).thenReturn(Optional.of(expPayment));
        assertEquals(expPayment, service.submitPayment(buildCommand()));
    }

    private SubmitPaymentCommand buildCommand() {
        return new SubmitPaymentCommand("",
            AUTHORISED_CARD_NUMBER,
            AUTHORISED_CARD_CVV,
            AUTHORISED_CARD_EXPIRY_MONTH,
            AUTHORISED_CARD_EXPIRY_YEAR,
            SupportedCurrency.valueOf(AUTHORISED_CARD_CURRENCY),
            AUTHORISED_CARD_AMOUNT
        );
    }

    private Payment buildPayment() {
        return new Payment("",
            UUID.randomUUID(),
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