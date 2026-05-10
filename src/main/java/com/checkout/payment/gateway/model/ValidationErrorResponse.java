package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Response model for validation errors (Rejected status).
 */
public class ValidationErrorResponse {

  private PaymentStatus status;

  @JsonProperty("failure_reason")
  private String failureReason;

  private List<FieldError> errors;

  public ValidationErrorResponse() {
    this.status = PaymentStatus.REJECTED;
    this.failureReason = "validation_failed";
  }

  public ValidationErrorResponse(List<FieldError> errors) {
    this();
    this.errors = errors;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public List<FieldError> getErrors() {
    return errors;
  }

  public void setErrors(List<FieldError> errors) {
    this.errors = errors;
  }

  /**
   * Represents a single field validation error.
   */
  public static class FieldError {
    private String field;
    private String message;
    private Object rejectedValue;

    public FieldError() {
    }

    public FieldError(String field, String message, Object rejectedValue) {
      this.field = field;
      this.message = message;
      this.rejectedValue = rejectedValue;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public Object getRejectedValue() {
      return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
      this.rejectedValue = rejectedValue;
    }
  }
}
