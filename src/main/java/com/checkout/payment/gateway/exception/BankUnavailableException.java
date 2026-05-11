package com.checkout.payment.gateway.exception;

import java.util.UUID;

public class BankUnavailableException extends RuntimeException {

  private UUID paymentId;

  public BankUnavailableException(String message) {
    super(message);
  }

  public BankUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public BankUnavailableException(String message, UUID paymentId) {
    super(message);
    this.paymentId = paymentId;
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(UUID paymentId) {
    this.paymentId = paymentId;
  }
}
