package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.model.bank.BankPaymentRequest;
import com.checkout.payment.gateway.model.bank.BankPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class BankClientTest {

  @Mock
  private RestTemplate restTemplate;

  private BankClient bankClient;

  private final String bankApiUrl = "http://localhost:8080/payments";

  @BeforeEach
  void setUp() {
    bankClient = new BankClient(restTemplate, bankApiUrl);
  }

  @Test
  void testSubmitPaymentSuccess() {
    BankPaymentRequest request = new BankPaymentRequest(
        "4111111111111111",
        "12/25",
        "USD",
        1000,
        "123"
    );

    BankPaymentResponse expectedResponse = new BankPaymentResponse(true, "AUTH123");

    when(restTemplate.postForObject(bankApiUrl, request, BankPaymentResponse.class))
        .thenReturn(expectedResponse);

    BankPaymentResponse actualResponse = bankClient.submitPayment(request);

    assertNotNull(actualResponse);
    assertTrue(actualResponse.isAuthorized());
    assertEquals("AUTH123", actualResponse.getAuthorizationCode());

    verify(restTemplate).postForObject(bankApiUrl, request, BankPaymentResponse.class);
  }

  @Test
  void testSubmitPaymentWhenBankDeclines() {
    BankPaymentRequest request = new BankPaymentRequest(
        "4111111111111111",
        "12/25",
        "USD",
        1000,
        "123"
    );

    BankPaymentResponse expectedResponse = new BankPaymentResponse(false, "DECLINED");

    when(restTemplate.postForObject(bankApiUrl, request, BankPaymentResponse.class))
        .thenReturn(expectedResponse);

    BankPaymentResponse actualResponse = bankClient.submitPayment(request);

    assertNotNull(actualResponse);
    assertFalse(actualResponse.isAuthorized());
    assertEquals("DECLINED", actualResponse.getAuthorizationCode());

    verify(restTemplate).postForObject(bankApiUrl, request, BankPaymentResponse.class);
  }

  @Test
  void testSubmitPaymentWhenServiceUnavailable() {
    BankPaymentRequest request = new BankPaymentRequest(
        "4111111111111111",
        "12/25",
        "USD",
        1000,
        "123"
    );

    when(restTemplate.postForObject(bankApiUrl, request, BankPaymentResponse.class))
        .thenThrow(ServiceUnavailable.class);

    BankUnavailableException exception = assertThrows(
        BankUnavailableException.class,
        () -> bankClient.submitPayment(request)
    );

    assertEquals("Bank service is unavailable", exception.getMessage());

    verify(restTemplate).postForObject(bankApiUrl, request, BankPaymentResponse.class);
  }
}
