package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.model.bank.BankPaymentRequest;
import com.checkout.payment.gateway.model.bank.BankPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.RestTemplate;

@Service
public class BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClient.class);

  private final RestTemplate restTemplate;
  private final String bankApiUrl;

  public BankClient(RestTemplate restTemplate, @Value("${bank.api.url}") String bankApiUrl) {
    this.restTemplate = restTemplate;
    this.bankApiUrl = bankApiUrl;
  }

  public BankPaymentResponse submitPayment(BankPaymentRequest request) {
    LOG.info("Submitting payment to bank: {}", request);

    try {
      BankPaymentResponse response = restTemplate.postForObject(
          bankApiUrl,
          request,
          BankPaymentResponse.class
      );

      LOG.info("Received response from bank: {}", response);

      return response;
    } catch (ServiceUnavailable e) {
      LOG.error("Bank service unavailable: {}", e.getMessage(), e);
      throw new BankUnavailableException("Bank service is unavailable", e);
    }
  }
}
