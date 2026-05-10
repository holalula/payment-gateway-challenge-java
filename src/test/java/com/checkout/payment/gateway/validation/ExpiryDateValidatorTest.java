package com.checkout.payment.gateway.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpiryDateValidatorTest {

  private ExpiryDateValidator validator;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new ExpiryDateValidator();
    context = mock(ConstraintValidatorContext.class);
  }

  @Test
  void whenRequestIsNull_thenValid() {
    boolean result = validator.isValid(null, context);
    assertTrue(result, "Null request should be considered valid (let @NotNull handle it)");
  }

  @Test
  void whenExpiryMonthIsNull_thenValid() {
    PostPaymentRequest request = createRequest(null, 2025);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Null expiry month should be considered valid (let @NotNull handle it)");
  }

  @Test
  void whenExpiryYearIsNull_thenValid() {
    PostPaymentRequest request = createRequest(12, null);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Null expiry year should be considered valid (let @NotNull handle it)");
  }

  @Test
  void whenBothExpiryMonthAndYearNull_thenValid() {
    PostPaymentRequest request = createRequest(null, null);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Null values should be considered valid (let @NotNull handle them)");
  }

  @Test
  void whenExpiryDateInPast_thenInvalid() {
    PostPaymentRequest request = createRequest(1, 2020);
    boolean result = validator.isValid(request, context);
    assertFalse(result, "Past expiry date should be invalid");
  }

  @Test
  void whenExpiryDateLastMonth_thenInvalid() {
    YearMonth lastMonth = YearMonth.now().minusMonths(1);
    PostPaymentRequest request = createRequest(lastMonth.getMonthValue(), lastMonth.getYear());
    boolean result = validator.isValid(request, context);
    assertFalse(result, "Last month should be invalid");
  }

  @Test
  void whenExpiryDateIsCurrentMonth_thenValid() {
    YearMonth now = YearMonth.now();
    PostPaymentRequest request = createRequest(now.getMonthValue(), now.getYear());
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Current month should be valid");
  }

  @Test
  void whenExpiryDateNextMonth_thenValid() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);
    PostPaymentRequest request = createRequest(nextMonth.getMonthValue(), nextMonth.getYear());
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Next month should be valid");
  }

  @Test
  void whenExpiryDateInFuture_thenValid() {
    PostPaymentRequest request = createRequest(12, 2030);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Future date should be valid");
  }

  @Test
  void whenExpiryDateFarInFuture_thenValid() {
    PostPaymentRequest request = createRequest(1, 2099);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "Far future date should be valid");
  }

  @Test
  void whenInvalidMonth_thenInvalid() {
    PostPaymentRequest request = createRequest(13, 2025);
    boolean result = validator.isValid(request, context);
    assertFalse(result, "Invalid month (13) should be invalid");
  }

  @Test
  void whenInvalidMonthZero_thenInvalid() {
    PostPaymentRequest request = createRequest(0, 2025);
    boolean result = validator.isValid(request, context);
    assertFalse(result, "Invalid month (0) should be invalid");
  }

  @Test
  void whenNegativeMonth_thenInvalid() {
    PostPaymentRequest request = createRequest(-1, 2025);
    boolean result = validator.isValid(request, context);
    assertFalse(result, "Negative month should be invalid");
  }

  @Test
  void whenExpiryDateDecember_thenValid() {
    int currentYear = YearMonth.now().getYear();
    PostPaymentRequest request = createRequest(12, currentYear + 1);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "December of next year should be valid");
  }

  @Test
  void whenExpiryDateJanuary_thenValid() {
    int currentYear = YearMonth.now().getYear();
    PostPaymentRequest request = createRequest(1, currentYear + 1);
    boolean result = validator.isValid(request, context);
    assertTrue(result, "January of next year should be valid");
  }

  @Test
  void whenCurrentYearPastMonth_thenInvalid() {
    YearMonth now = YearMonth.now();
    if (now.getMonthValue() > 1) {
      PostPaymentRequest request = createRequest(now.getMonthValue() - 1, now.getYear());
      boolean result = validator.isValid(request, context);
      assertFalse(result, "Past month of current year should be invalid");
    }
  }

  @Test
  void whenCurrentYearFutureMonth_thenValid() {
    YearMonth now = YearMonth.now();
    if (now.getMonthValue() < 12) {
      PostPaymentRequest request = createRequest(now.getMonthValue() + 1, now.getYear());
      boolean result = validator.isValid(request, context);
      assertTrue(result, "Future month of current year should be valid");
    }
  }

  private PostPaymentRequest createRequest(Integer expiryMonth, Integer expiryYear) {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setExpiryMonth(expiryMonth);
    request.setExpiryYear(expiryYear);
    // Set other required fields to avoid null validation issues in real scenarios
    request.setCardNumber("4111111111111111");
    request.setCurrency("USD");
    request.setAmount(1000);
    request.setCvv("123");
    return request;
  }
}
