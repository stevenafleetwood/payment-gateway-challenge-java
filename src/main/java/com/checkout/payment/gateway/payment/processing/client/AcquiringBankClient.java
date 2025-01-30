package com.checkout.payment.gateway.payment.processing.client;

import com.checkout.payment.gateway.configuration.AcquiringBankProperties;
import com.checkout.payment.gateway.payment.processing.exception.PaymentProcessingException;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AcquiringBankClient {

  private final String host;
  private final RestTemplate restTemplate;

  public AcquiringBankClient(AcquiringBankProperties acquiringBankProperties,
      RestTemplate restTemplate) {
    this.host = acquiringBankProperties.getHost();
    this.restTemplate = restTemplate;
  }

  @Timed("acquiring.bank.authorise.payment") //Likely a useful metric
  public PaymentResponse authorisePayment(PaymentRequest request) {
    try {
      return this.restTemplate
          .exchange(
              this.host + "/payments", HttpMethod.POST, new HttpEntity<>(request),
              PaymentResponse.class
          )
          .getBody();
    } catch (RestClientException ex) {
      String errMsg = "Unexpected response from Acquiring Bank";
      log.error(errMsg, ex);
      throw new PaymentProcessingException(errMsg, ex);
    }
  }

  ;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PaymentRequest {

    @JsonSetter("card_number")
    String cardNumber;
    @JsonSetter("expiry_date")
    String expiryDate;
    String currency;
    Integer amount;
    String cvv;
  }


  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PaymentResponse {

    Boolean authorized;
    @JsonSetter("authorization_code")
    //Review comment - This looks like a UUID in the requirements document, but is not explicitly
    // called out, so safer to stick with string
    String authorizationCode;
  }
}
