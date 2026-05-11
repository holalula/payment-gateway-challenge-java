package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.ValidationErrorResponse;
import com.checkout.payment.gateway.model.ValidationErrorResponse.FieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(
      PaymentNotFoundException ex) {
    LOG.warn("Payment not found: {}", ex.getMessage());
    return new ResponseEntity<>(
        new ErrorResponse(String.format("Payment not found with ID: %s", ex.getPaymentId())),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    LOG.warn("Invalid parameter type: {}", ex.getMessage());
    String message = String.format("Invalid Payment ID format for parameter '%s': %s",
        ex.getName(), ex.getValue());
    return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<PostPaymentResponse> handleBankUnavailableException(
      BankUnavailableException ex) {
    LOG.error("Bank unavailable exception: {}", ex.getMessage(), ex);

    PostPaymentResponse response = new PostPaymentResponse();
    response.setId(ex.getPaymentId());
    response.setStatus(PaymentStatus.FAILED);
    response.setFailureReason("bank_unavailable");

    return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    LOG.warn("Validation error occurred: {}", ex.getMessage());

    List<FieldError> errors = new ArrayList<>();

    // Handle field errors
    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errors.add(new FieldError(
          error.getField(),
          error.getDefaultMessage(),
          error.getRejectedValue()
      ));
    });

    // Handle custom validation errors
    ex.getBindingResult().getGlobalErrors().forEach(error -> {
      errors.add(new FieldError(
          error.getObjectName(),
          error.getDefaultMessage(),
          getGlobalErrorValue(error)
      ));
    });

    ValidationErrorResponse response = new ValidationErrorResponse(errors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex) {
    LOG.warn("Constraint violation occurred: {}", ex.getMessage());

    List<FieldError> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      errors.add(new FieldError(
          violation.getPropertyPath().toString(),
          violation.getMessage(),
          violation.getInvalidValue()
      ));
    }

    ValidationErrorResponse response = new ValidationErrorResponse(errors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  private Object getGlobalErrorValue(ObjectError error) {
    // TODO: Return combination of field names for a custom error
    return error.getObjectName();
  }
}
