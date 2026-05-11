package com.checkout.payment.gateway.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

  private final UUID paymentId;

  public PaymentNotFoundException(UUID paymentId) {
    super(String.format("Payment not found with ID: %s", paymentId));
    this.paymentId = paymentId;
  }

  public UUID getPaymentId() {
    return paymentId;
  }
}
