package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;

/**
 * Validator implementation for the @ValidExpiryDate annotation.
 * Validates that the combination of expiry month and year is in the future.
 */
public class ExpiryDateValidator implements ConstraintValidator<ValidExpiryDate, PostPaymentRequest> {

  @Override
  public void initialize(ValidExpiryDate constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(PostPaymentRequest request, ConstraintValidatorContext context) {
    // If either field is null, let @NotNull handle that validation
    if (request == null || request.getExpiryMonth() == null || request.getExpiryYear() == null) {
      return true;
    }

    try {
      YearMonth expiryDate = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
      YearMonth currentDate = YearMonth.now();

      // The expiry date must be in the current month or later
      return !expiryDate.isBefore(currentDate);
    } catch (Exception e) {
      // If we can't create a valid YearMonth, the validation fails
      return false;
    }
  }
}
