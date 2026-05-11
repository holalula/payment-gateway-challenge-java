package com.checkout.payment.gateway.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.ValidationErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class CommonExceptionHandlerTest {

  @InjectMocks
  private CommonExceptionHandler exceptionHandler;

  @Test
  void testHandleEventProcessingException() {
    EventProcessingException exception = new EventProcessingException("Test error");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Page not found", response.getBody().getMessage());
  }

  @Test
  void testHandlePaymentNotFoundException() {
    UUID paymentId = UUID.randomUUID();
    PaymentNotFoundException exception = new PaymentNotFoundException(paymentId);

    ResponseEntity<ErrorResponse> response = exceptionHandler.handlePaymentNotFoundException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Payment not found with ID: " + paymentId, response.getBody().getMessage());
  }

  @Test
  void testHandleMethodArgumentTypeMismatchException() {
    MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
    when(exception.getName()).thenReturn("id");
    when(exception.getValue()).thenReturn("invalid-uuid");
    when(exception.getMessage()).thenReturn("Type mismatch");

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleMethodArgumentTypeMismatchException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getMessage().contains("Invalid Payment ID format"));
    assertTrue(response.getBody().getMessage().contains("invalid-uuid"));
  }

  @Test
  void testHandleBankUnavailableException() {
    UUID paymentId = UUID.randomUUID();
    BankUnavailableException exception = new BankUnavailableException("Bank is down", paymentId);

    ResponseEntity<PostPaymentResponse> response =
        exceptionHandler.handleBankUnavailableException(exception);

    assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(paymentId, response.getBody().getId());
    assertEquals(PaymentStatus.FAILED, response.getBody().getStatus());
    assertEquals("bank_unavailable", response.getBody().getFailureReason());
  }

  @Test
  void testHandleValidationExceptionWithFieldErrors() {
    BindingResult bindingResult = mock(BindingResult.class);
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);

    FieldError fieldError1 = new FieldError("postPaymentRequest", "cardNumber", "1234",
        false, null, null, "Card number is invalid");
    FieldError fieldError2 = new FieldError("postPaymentRequest", "amount", null,
        false, null, null, "Amount must not be null");

    when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));
    when(bindingResult.getGlobalErrors()).thenReturn(java.util.List.of());

    ResponseEntity<ValidationErrorResponse> response =
        exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getErrors().size());

    ValidationErrorResponse.FieldError error1 = response.getBody().getErrors().get(0);
    assertEquals("cardNumber", error1.getField());
    assertEquals("Card number is invalid", error1.getMessage());

    ValidationErrorResponse.FieldError error2 = response.getBody().getErrors().get(1);
    assertEquals("amount", error2.getField());
    assertEquals("Amount must not be null", error2.getMessage());
  }

  @Test
  void testHandleValidationExceptionWithGlobalErrors() {
    BindingResult bindingResult = mock(BindingResult.class);
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);

    ObjectError globalError = new ObjectError("postPaymentRequest", "Object validation failed");

    when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());
    when(bindingResult.getGlobalErrors()).thenReturn(java.util.List.of(globalError));

    ResponseEntity<ValidationErrorResponse> response =
        exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getErrors().size());

    ValidationErrorResponse.FieldError error = response.getBody().getErrors().get(0);
    assertEquals("postPaymentRequest", error.getField());
    assertEquals("Object validation failed", error.getMessage());
  }

  @Test
  void testHandleConstraintViolationException() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();

    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("cardNumber");
    when(violation1.getPropertyPath()).thenReturn(path1);
    when(violation1.getMessage()).thenReturn("must not be blank");
    when(violation1.getInvalidValue()).thenReturn("");

    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("amount");
    when(violation2.getPropertyPath()).thenReturn(path2);
    when(violation2.getMessage()).thenReturn("must be positive");
    when(violation2.getInvalidValue()).thenReturn(-100);

    violations.add(violation1);
    violations.add(violation2);

    ConstraintViolationException exception = new ConstraintViolationException(violations);

    ResponseEntity<ValidationErrorResponse> response =
        exceptionHandler.handleConstraintViolation(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getErrors().size());
  }
}
