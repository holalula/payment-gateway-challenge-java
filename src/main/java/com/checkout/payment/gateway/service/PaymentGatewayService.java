package com.checkout.payment.gateway.service;

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

  public PaymentGatewayService(PaymentsRepository paymentsRepository) {
    this.paymentsRepository = paymentsRepository;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  /**
   * Process a payment request.
   * This is a stub implementation that always returns AUTHORIZED status.
   * TODO: Integrate with bank client to get actual authorization status.
   *
   * @param paymentRequest the payment request
   * @return payment response with status
   */
  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    LOG.info("Processing payment request: {}", paymentRequest);

    // Generate payment ID
    UUID paymentId = UUID.randomUUID();

    // Extract last 4 digits of card number
    String cardNumber = paymentRequest.getCardNumber();
    String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

    // Create response (stub implementation - always AUTHORIZED for now)
    PostPaymentResponse response = new PostPaymentResponse();
    response.setId(paymentId);
    response.setStatus(PaymentStatus.AUTHORIZED);
    response.setCardNumberLastFour(Integer.parseInt(lastFourDigits));
    response.setExpiryMonth(paymentRequest.getExpiryMonth());
    response.setExpiryYear(paymentRequest.getExpiryYear());
    response.setCurrency(paymentRequest.getCurrency());
    response.setAmount(paymentRequest.getAmount());

    // Store the created payment
    paymentsRepository.add(response);

    LOG.info("Payment processed successfully with ID: {}", paymentId);

    return response;
  }
}
