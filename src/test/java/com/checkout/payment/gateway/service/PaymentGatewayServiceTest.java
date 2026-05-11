package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.bank.BankPaymentRequest;
import com.checkout.payment.gateway.model.bank.BankPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private BankClient bankClient;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  private PostPaymentRequest paymentRequest;

  @BeforeEach
  void setUp() {
    paymentRequest = new PostPaymentRequest();
    paymentRequest.setCardNumber("4111111111111111");
    paymentRequest.setExpiryMonth(12);
    paymentRequest.setExpiryYear(2025);
    paymentRequest.setCurrency("USD");
    paymentRequest.setAmount(1000);
    paymentRequest.setCvv("123");
  }

  @Test
  void testGetPaymentByIdWhenPaymentExists() {
    UUID paymentId = UUID.randomUUID();
    PostPaymentResponse storedPayment = new PostPaymentResponse();
    storedPayment.setId(paymentId);
    storedPayment.setStatus(PaymentStatus.AUTHORIZED);
    storedPayment.setCardNumberLastFour(1111);
    storedPayment.setExpiryMonth(12);
    storedPayment.setExpiryYear(2025);
    storedPayment.setCurrency("USD");
    storedPayment.setAmount(1000);

    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(storedPayment));

    GetPaymentResponse response = paymentGatewayService.getPaymentById(paymentId);

    assertNotNull(response);
    assertEquals(paymentId, response.getId());
    assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    assertEquals(1111, response.getCardNumberLastFour());
    assertEquals(12, response.getExpiryMonth());
    assertEquals(2025, response.getExpiryYear());
    assertEquals("USD", response.getCurrency());
    assertEquals(1000, response.getAmount());

    verify(paymentsRepository).get(paymentId);
  }

  @Test
  void testGetPaymentByIdWhenPaymentNotFound() {
    UUID paymentId = UUID.randomUUID();

    when(paymentsRepository.get(paymentId)).thenReturn(Optional.empty());

    PaymentNotFoundException exception = assertThrows(
        PaymentNotFoundException.class,
        () -> paymentGatewayService.getPaymentById(paymentId)
    );

    assertEquals(paymentId, exception.getPaymentId());
    verify(paymentsRepository).get(paymentId);
  }

  @Test
  void testProcessPaymentWhenBankAuthorizes() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(true, "AUTH123");
    when(bankClient.submitPayment(any(BankPaymentRequest.class))).thenReturn(bankResponse);

    PostPaymentResponse response = paymentGatewayService.processPayment(paymentRequest);

    assertNotNull(response);
    assertNotNull(response.getId());
    assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    assertEquals(1111, response.getCardNumberLastFour());
    assertEquals(12, response.getExpiryMonth());
    assertEquals(2025, response.getExpiryYear());
    assertEquals("USD", response.getCurrency());
    assertEquals(1000, response.getAmount());

    verify(paymentsRepository, times(2)).add(any(PostPaymentResponse.class));
    verify(bankClient).submitPayment(any(BankPaymentRequest.class));
  }

  @Test
  void testProcessPaymentWhenBankDeclines() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(false, "DECLINED");
    when(bankClient.submitPayment(any(BankPaymentRequest.class))).thenReturn(bankResponse);

    PostPaymentResponse response = paymentGatewayService.processPayment(paymentRequest);

    assertNotNull(response);
    assertNotNull(response.getId());
    assertEquals(PaymentStatus.DECLINED, response.getStatus());
    assertEquals(1111, response.getCardNumberLastFour());

    verify(paymentsRepository, times(2)).add(any(PostPaymentResponse.class));
    verify(bankClient).submitPayment(any(BankPaymentRequest.class));
  }

  @Test
  void testProcessPaymentWhenBankUnavailable() {
    when(bankClient.submitPayment(any(BankPaymentRequest.class)))
        .thenThrow(new BankUnavailableException("Bank service is unavailable"));

    BankUnavailableException exception = assertThrows(
        BankUnavailableException.class,
        () -> paymentGatewayService.processPayment(paymentRequest)
    );

    assertNotNull(exception.getPaymentId());

    ArgumentCaptor<PostPaymentResponse> paymentCaptor = ArgumentCaptor.forClass(PostPaymentResponse.class);
    verify(paymentsRepository, times(2)).add(paymentCaptor.capture());

    PostPaymentResponse failedPayment = paymentCaptor.getValue();
    assertEquals(PaymentStatus.FAILED, failedPayment.getStatus());
    assertEquals("bank_unavailable", failedPayment.getFailureReason());

    verify(bankClient).submitPayment(any(BankPaymentRequest.class));
  }

  @Test
  void testProcessPaymentCreatesCorrectBankRequest() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(true, "AUTH123");
    when(bankClient.submitPayment(any(BankPaymentRequest.class))).thenReturn(bankResponse);

    paymentGatewayService.processPayment(paymentRequest);

    ArgumentCaptor<BankPaymentRequest> bankRequestCaptor = ArgumentCaptor.forClass(BankPaymentRequest.class);
    verify(bankClient).submitPayment(bankRequestCaptor.capture());

    BankPaymentRequest capturedRequest = bankRequestCaptor.getValue();
    assertEquals("4111111111111111", capturedRequest.getCardNumber());
    assertEquals("12/2025", capturedRequest.getExpiryDate());
    assertEquals("USD", capturedRequest.getCurrency());
    assertEquals(1000, capturedRequest.getAmount());
    assertEquals("123", capturedRequest.getCvv());
  }

  @Test
  void testProcessPaymentExtractsLastFourDigitsCorrectly() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(true, "AUTH123");
    when(bankClient.submitPayment(any(BankPaymentRequest.class))).thenReturn(bankResponse);

    paymentRequest.setCardNumber("5500000000000004");

    PostPaymentResponse response = paymentGatewayService.processPayment(paymentRequest);

    assertEquals(4, response.getCardNumberLastFour());
  }
}
