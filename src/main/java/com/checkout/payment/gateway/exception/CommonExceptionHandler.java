package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
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

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        HttpStatus.NOT_FOUND);
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
    // For global errors, we don't have a specific field value
    // Return the object name or a placeholder
    return error.getObjectName();
  }
}
