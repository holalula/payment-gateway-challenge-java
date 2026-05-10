package com.checkout.payment.gateway.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.YearMonth;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PostPaymentRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void whenAllFieldsValid_thenNoConstraintViolations() {
    PostPaymentRequest request = createValidRequest();
    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // Card number validation tests
  @Test
  void whenCardNumberIsNull_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber(null);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Card number is required")));
  }

  @Test
  void whenCardNumberTooShort_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber("1234567890123"); // 13 digits

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Card number must be 14-19 numeric digits")));
  }

  @Test
  void whenCardNumberTooLong_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber("12345678901234567890"); // 20 digits

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Card number must be 14-19 numeric digits")));
  }

  @Test
  void whenCardNumberContainsNonNumeric_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber("411111111111abcd");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Card number must be 14-19 numeric digits")));
  }

  @Test
  void whenCardNumber14Digits_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber("12345678901234"); // 14 digits

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenCardNumber19Digits_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber("1234567890123456789"); // 19 digits

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // Expiry month validation tests
  @Test
  void whenExpiryMonthIsNull_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryMonth(null);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Expiry month is required")));
  }

  @Test
  void whenExpiryMonthLessThan1_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryMonth(0);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Expiry month must be between 1 and 12")));
  }

  @Test
  void whenExpiryMonthGreaterThan12_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryMonth(13);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Expiry month must be between 1 and 12")));
  }

  @Test
  void whenExpiryMonth1_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryMonth(1);
    request.setExpiryYear(YearMonth.now().getYear() + 1);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenExpiryMonth12_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryMonth(12);
    request.setExpiryYear(YearMonth.now().getYear() + 1);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // Expiry year validation tests
  @Test
  void whenExpiryYearIsNull_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryYear(null);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Expiry year is required")));
  }

  @Test
  void whenExpiryDateInPast_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setExpiryMonth(1);
    request.setExpiryYear(2020);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Card expiry date must be in the future")));
  }

  @Test
  void whenExpiryDateIsCurrentMonth_thenValid() {
    PostPaymentRequest request = createValidRequest();
    YearMonth now = YearMonth.now();
    request.setExpiryMonth(now.getMonthValue());
    request.setExpiryYear(now.getYear());

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenExpiryDateInFuture_thenValid() {
    PostPaymentRequest request = createValidRequest();
    YearMonth futureDate = YearMonth.now().plusYears(5);
    request.setExpiryMonth(futureDate.getMonthValue());
    request.setExpiryYear(futureDate.getYear());

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // Currency validation tests
  @Test
  void whenCurrencyIsNull_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency(null);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Currency is required")));
  }

  @Test
  void whenCurrencyTooShort_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("US");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Currency must be 3 characters")));
  }

  @Test
  void whenCurrencyTooLong_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("USDD");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Currency must be 3 characters")));
  }

  @Test
  void whenCurrencyNotSupported_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("JPY");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Currency must be one of: USD, GBP, EUR")));
  }

  @Test
  void whenCurrencyUSD_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("USD");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenCurrencyGBP_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("GBP");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenCurrencyEUR_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("EUR");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // Amount validation tests
  @Test
  void whenAmountIsNull_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setAmount(null);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Amount is required")));
  }

  @Test
  void whenAmountIsZero_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setAmount(0);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Amount must be a positive integer")));
  }

  @Test
  void whenAmountIsNegative_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setAmount(-100);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Amount must be a positive integer")));
  }

  @Test
  void whenAmountIsPositive_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setAmount(1050);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // CVV validation tests
  @Test
  void whenCvvIsNull_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv(null);

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("CVV is required")));
  }

  @Test
  void whenCvvTooShort_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("12");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("CVV must be 3-4 numeric digits")));
  }

  @Test
  void whenCvvTooLong_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("12345");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("CVV must be 3-4 numeric digits")));
  }

  @Test
  void whenCvvContainsNonNumeric_thenConstraintViolation() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("12a");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("CVV must be 3-4 numeric digits")));
  }

  @Test
  void whenCvv3Digits_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("123");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void whenCvv4Digits_thenValid() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("1234");

    Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  // toString masking tests
  @Test
  void whenToStringCalled_thenSensitiveDataMasked() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber("4111111111111111");
    request.setCvv("123");

    String result = request.toString();

    assertTrue(result.contains("****1111"), "Card number should be masked");
    assertTrue(result.contains("cvv='***'"), "CVV should be masked");
    assertFalse(result.contains("4111111111111111"), "Full card number should not be in toString");
    assertFalse(result.contains("123"), "CVV value should not be in toString");
  }

  @Test
  void whenCardNumberNull_thenToStringHandlesGracefully() {
    PostPaymentRequest request = createValidRequest();
    request.setCardNumber(null);

    String result = request.toString();

    assertTrue(result.contains("****"), "Should handle null card number");
  }

  // Helper method
  private PostPaymentRequest createValidRequest() {
    PostPaymentRequest request = new PostPaymentRequest();
    YearMonth futureDate = YearMonth.now().plusYears(1);
    request.setCardNumber("4111111111111111");
    request.setExpiryMonth(futureDate.getMonthValue());
    request.setExpiryYear(futureDate.getYear());
    request.setCurrency("USD");
    request.setAmount(1050);
    request.setCvv("123");
    return request;
  }
}
