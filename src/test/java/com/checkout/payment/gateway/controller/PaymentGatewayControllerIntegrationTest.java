package com.checkout.payment.gateway.controller;


import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_AMOUNT;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CURRENCY;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_CVV;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_MONTH;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_EXPIRY_YEAR;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_LAST_4_NUMBERS;
import static com.checkout.payment.gateway.TestConstants.AUTHORISED_CARD_NUMBER;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_AMOUNT;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_CURRENCY;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_CVV;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_EXPIRY_MONTH;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_EXPIRY_YEAR;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_LAST_4_NUMBERS;
import static com.checkout.payment.gateway.TestConstants.DECLINED_CARD_NUMBER;
import static com.checkout.payment.gateway.TestConstants.OBJECT_MAPPER;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.api.resources.ErrorResponse;
import com.checkout.payment.gateway.api.resources.PaymentResponse;
import com.checkout.payment.gateway.api.resources.SubmitPaymentRequest;
import com.checkout.payment.gateway.api.resources.enums.Currency;
import com.fasterxml.jackson.core.JsonProcessingException;
import config.TestTimeConfiguration;
import java.io.File;
import java.util.UUID;
import java.util.stream.Stream;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.ComposeContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestTimeConfiguration.class)
class PaymentGatewayControllerIntegrationTest {

  //Review comment - Please note the use of TestContainers here, the system executing test must be
  // running docker

  @Autowired
  private MockMvc mvc;

  private static final String BANK_SIMULATOR_SERVICE_NAME = "bank_simulator";
  private static final int BANK_SIMULATOR_SERVICE_PORT = 8080;
  private static final ComposeContainer BANK_SIMULATOR = new ComposeContainer(
      new File("docker-compose.yml")
  )
      .withExposedService(BANK_SIMULATOR_SERVICE_NAME, BANK_SIMULATOR_SERVICE_PORT);

  @BeforeAll
  public static void setup() {
    BANK_SIMULATOR.start();
  }

  @AfterAll
  public static void teardown() {
    BANK_SIMULATOR.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("acquiring.bank.host",
        () -> "http://" + BANK_SIMULATOR.getServiceHost("bank_simulator",
            BANK_SIMULATOR_SERVICE_PORT) + ":" + BANK_SIMULATOR.getServicePort(
            BANK_SIMULATOR_SERVICE_NAME, BANK_SIMULATOR_SERVICE_PORT));
  }

  public static Stream<Arguments> invalidPaymentSubmissionRequests()
      throws JsonProcessingException, JSONException {
    return Stream.of(
        Arguments.of(validSubmitPaymentRequestJson().put("card_number", "1234567890123"),
            "Credit card number is invalid"),
        Arguments.of(validSubmitPaymentRequestJson().put("card_number", "12345678901234567890"),
            "Credit card number is invalid"),
        Arguments.of(validSubmitPaymentRequestJson().put("card_number", "12345678901234A5"),
            "Credit card number is invalid"),
        Arguments.of(validSubmitPaymentRequestJson().put("card_number", null),
            "Credit card number is invalid"),
        Arguments.of(validSubmitPaymentRequestJson().put("expiry_month", 0),
            "Credit card expiry is invalid"),
        Arguments.of(validSubmitPaymentRequestJson().put("expiry_month", 13),
            "Credit card expiry is invalid"),
        Arguments.of(validSubmitPaymentRequestJson().put("expiry_month", null),
            "Credit card expiry is invalid"),
        Arguments.of(
            validSubmitPaymentRequestJson().put("expiry_month",
                TestTimeConfiguration.FIXED_MONTH - 1), "Credit card expiry is invalid"),
        Arguments.of(
            validSubmitPaymentRequestJson().put("expiry_year",
                TestTimeConfiguration.FIXED_YEAR - 1), "Credit card expiry is invalid")
        //TODO - Exercise all validations, I have run out of time
    );
  }

  @ParameterizedTest
  @MethodSource("invalidPaymentSubmissionRequests")
  void paymentSubmissionValidation_InvalidRequest(JSONObject requestJson, String expError)
      throws Exception {
    var response = mvc.perform(MockMvcRequestBuilders.post("/submitPayment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson.toString(2)))
        .andExpect(status().isBadRequest())
        .andReturn();

    ErrorResponse errorResponse = OBJECT_MAPPER.readValue(
        response.getResponse().getContentAsString(), ErrorResponse.class);
    assertTrue(errorResponse.getMessage().contains(expError),
        "Error response doesn't contain error: " + expError);

  }

  @Test
  void testSubmissionAndRetrievalOfApprovedPayment() throws Exception {
    SubmitPaymentRequest request = authorisedSubmitPaymentRequest();
    MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.post("/submitPayment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number").value(AUTHORISED_CARD_LAST_4_NUMBERS))
        .andExpect(jsonPath("$.expiry_month").value(request.getExpiryMonth()))
        .andExpect(jsonPath("$.expiry_year").value(request.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(request.getCurrency().toString()))
        .andExpect(jsonPath("$.amount").value(request.getAmount()))
        .andReturn().getResponse();

    UUID createdId = OBJECT_MAPPER.readValue(response.getContentAsString(), PaymentResponse.class)
        .getId();

    mvc.perform(MockMvcRequestBuilders.get("/retrievePayment/" + createdId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number").value(AUTHORISED_CARD_LAST_4_NUMBERS))
        .andExpect(jsonPath("$.expiry_month").value(request.getExpiryMonth()))
        .andExpect(jsonPath("$.expiry_year").value(request.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(request.getCurrency().toString()))
        .andExpect(jsonPath("$.amount").value(request.getAmount()));
  }

  @Test
  void testSubmissionAndRetrievalOfDeclinedPayment() throws Exception {
    SubmitPaymentRequest request = declinedSubmitPaymentRequest();
    MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.post("/submitPayment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.card_number").value(DECLINED_CARD_LAST_4_NUMBERS))
        .andExpect(jsonPath("$.expiry_month").value(request.getExpiryMonth()))
        .andExpect(jsonPath("$.expiry_year").value(request.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(request.getCurrency().toString()))
        .andExpect(jsonPath("$.amount").value(request.getAmount()))
        .andReturn().getResponse();

    UUID createdId = OBJECT_MAPPER.readValue(response.getContentAsString(), PaymentResponse.class)
        .getId();

    mvc.perform(MockMvcRequestBuilders.get("/retrievePayment/" + createdId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.card_number").value(DECLINED_CARD_LAST_4_NUMBERS))
        .andExpect(jsonPath("$.expiry_month").value(request.getExpiryMonth()))
        .andExpect(jsonPath("$.expiry_year").value(request.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(request.getCurrency().toString()))
        .andExpect(jsonPath("$.amount").value(request.getAmount()));
  }


  @Test
  void testSubmissionErrorsWithAcquiringBank() throws Exception {
    JSONObject json = validSubmitPaymentRequestJson().put("card_number", "123456789012345");
    mvc.perform(MockMvcRequestBuilders.post("/submitPayment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.toString(2)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    mvc.perform(MockMvcRequestBuilders.get("/retrievePayment/" + nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            "Request was invalid: No payment found with id: " + nonExistentId));
  }

  private static SubmitPaymentRequest authorisedSubmitPaymentRequest() {
    SubmitPaymentRequest.SubmitPaymentRequestBuilder builder = SubmitPaymentRequest.builder();
    builder.currency(Currency.valueOf(AUTHORISED_CARD_CURRENCY));
    builder.cvv(AUTHORISED_CARD_CVV);
    builder.amount(AUTHORISED_CARD_AMOUNT);
    builder.expiryYear(AUTHORISED_CARD_EXPIRY_YEAR);
    builder.expiryMonth(AUTHORISED_CARD_EXPIRY_MONTH);
    builder.cardNumber(AUTHORISED_CARD_NUMBER);
    return builder.build();
  }

  private static SubmitPaymentRequest declinedSubmitPaymentRequest() {
    SubmitPaymentRequest.SubmitPaymentRequestBuilder builder = SubmitPaymentRequest.builder();
    builder.currency(Currency.valueOf(DECLINED_CARD_CURRENCY));
    builder.cvv(DECLINED_CARD_CVV);
    builder.amount(DECLINED_CARD_AMOUNT);
    builder.expiryYear(DECLINED_CARD_EXPIRY_YEAR);
    builder.expiryMonth(DECLINED_CARD_EXPIRY_MONTH);
    builder.cardNumber(DECLINED_CARD_NUMBER);
    return builder.build();
  }

  private static JSONObject validSubmitPaymentRequestJson()
      throws JsonProcessingException, JSONException {
    return new JSONObject(OBJECT_MAPPER.writeValueAsString(authorisedSubmitPaymentRequest()));
  }
}
