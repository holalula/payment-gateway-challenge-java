package com.checkout.payment.gateway.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure that the expiry month and year combination is in the future.
 * This annotation should be applied at the class level on the request object.
 */
@Documented
@Constraint(validatedBy = ExpiryDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpiryDate {
  String message() default "Card expiry date must be in the future";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
