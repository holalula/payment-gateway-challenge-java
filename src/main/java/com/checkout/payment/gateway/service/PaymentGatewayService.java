package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.bank.BankPaymentRequest;
import com.checkout.payment.gateway.model.bank.BankPaymentResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.GetPaymentResponse;
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

  private static final String BANK_UNAVAILABLE = "bank_unavailable";

  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public GetPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);

    PostPaymentResponse payment = paymentsRepository.get(id)
        .orElseThrow(() -> new PaymentNotFoundException(id));

    return mapToGetPaymentResponse(payment);
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    LOG.info("Processing payment request: {}", paymentRequest);

    PostPaymentResponse payment = createPendingPayment(paymentRequest);

    paymentsRepository.add(payment);
    LOG.info("Payment created with ID: {} and status: PENDING", payment.getId());

    BankPaymentRequest bankRequest = createBankRequest(paymentRequest);

    try {
      BankPaymentResponse bankResponse = bankClient.submitPayment(bankRequest);

      PaymentStatus status = bankResponse.isAuthorized()
          ? PaymentStatus.AUTHORIZED
          : PaymentStatus.DECLINED;

      payment.setStatus(status);
      paymentsRepository.add(payment);

      LOG.info("Payment updated with ID: {} and status: {}", payment.getId(), status);

      return payment;
    } catch (BankUnavailableException e) {
      LOG.error("Bank unavailable for payment ID: {}", payment.getId(), e);

      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureReason(BANK_UNAVAILABLE);
      paymentsRepository.add(payment);

      LOG.info("Payment updated with ID: {} and status: FAILED", payment.getId());

      throw new BankUnavailableException("Bank service is unavailable", payment.getId());
    }
  }

  private PostPaymentResponse createPendingPayment(PostPaymentRequest paymentRequest) {
    UUID paymentId = UUID.randomUUID();

    String lastFourDigits = extractLastFourDigits(paymentRequest.getCardNumber());

    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(paymentId);
    payment.setStatus(PaymentStatus.PENDING);
    payment.setCardNumberLastFour(Integer.parseInt(lastFourDigits));
    payment.setExpiryMonth(paymentRequest.getExpiryMonth());
    payment.setExpiryYear(paymentRequest.getExpiryYear());
    payment.setCurrency(paymentRequest.getCurrency());
    payment.setAmount(paymentRequest.getAmount());

    return payment;
  }

  private String extractLastFourDigits(String cardNumber) {
    return cardNumber.substring(cardNumber.length() - 4);
  }

  private BankPaymentRequest createBankRequest(PostPaymentRequest paymentRequest) {
    return new BankPaymentRequest(
        paymentRequest.getCardNumber(),
        paymentRequest.getExpiryDate(),
        paymentRequest.getCurrency(),
        paymentRequest.getAmount(),
        paymentRequest.getCvv()
    );
  }

  private GetPaymentResponse mapToGetPaymentResponse(PostPaymentResponse payment) {
    GetPaymentResponse response = new GetPaymentResponse();
    response.setId(payment.getId());
    response.setStatus(payment.getStatus());
    response.setCardNumberLastFour(payment.getCardNumberLastFour());
    response.setExpiryMonth(payment.getExpiryMonth());
    response.setExpiryYear(payment.getExpiryYear());
    response.setCurrency(payment.getCurrency());
    response.setAmount(payment.getAmount());
    return response;
  }
}
