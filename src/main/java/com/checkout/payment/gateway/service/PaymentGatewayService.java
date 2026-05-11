package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.bank.BankPaymentRequest;
import com.checkout.payment.gateway.model.bank.BankPaymentResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    LOG.info("Processing payment request: {}", paymentRequest);

    // Generate payment ID
    UUID paymentId = UUID.randomUUID();

    // Extract last 4 digits of card number
    String cardNumber = paymentRequest.getCardNumber();
    String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

    // Create bank request
    BankPaymentRequest bankRequest = new BankPaymentRequest(
        paymentRequest.getCardNumber(),
        paymentRequest.getExpiryDate(),
        paymentRequest.getCurrency(),
        paymentRequest.getAmount(),
        paymentRequest.getCvv()
    );

    // Submit to bank
    BankPaymentResponse bankResponse = bankClient.submitPayment(bankRequest);

    // Determine payment status based on bank response
    PaymentStatus status = bankResponse.isAuthorized()
        ? PaymentStatus.AUTHORIZED
        : PaymentStatus.DECLINED;

    // Create payment response
    PostPaymentResponse response = new PostPaymentResponse();
    response.setId(paymentId);
    response.setStatus(status);
    response.setCardNumberLastFour(Integer.parseInt(lastFourDigits));
    response.setExpiryMonth(paymentRequest.getExpiryMonth());
    response.setExpiryYear(paymentRequest.getExpiryYear());
    response.setCurrency(paymentRequest.getCurrency());
    response.setAmount(paymentRequest.getAmount());

    // Store the created payment
    paymentsRepository.add(response);

    LOG.info("Payment processed successfully with ID: {} and status: {}", paymentId, status);

    return response;
  }
}
