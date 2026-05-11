package com.checkout.payment.gateway.model.bank;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BankPaymentRequestTest {

  @Test
  void testDefaultConstructor() {
    BankPaymentRequest request = new BankPaymentRequest();
    assertNotNull(request);
  }

  @Test
  void testParameterizedConstructor() {
    BankPaymentRequest request = new BankPaymentRequest(
        "4111111111111111",
        "12/25",
        "USD",
        1000,
        "123"
    );

    assertEquals("4111111111111111", request.getCardNumber());
    assertEquals("12/25", request.getExpiryDate());
    assertEquals("USD", request.getCurrency());
    assertEquals(1000, request.getAmount());
    assertEquals("123", request.getCvv());
  }

  @Test
  void testSettersAndGetters() {
    BankPaymentRequest request = new BankPaymentRequest();

    request.setCardNumber("5500000000000004");
    request.setExpiryDate("06/26");
    request.setCurrency("GBP");
    request.setAmount(2500);
    request.setCvv("456");

    assertEquals("5500000000000004", request.getCardNumber());
    assertEquals("06/26", request.getExpiryDate());
    assertEquals("GBP", request.getCurrency());
    assertEquals(2500, request.getAmount());
    assertEquals("456", request.getCvv());
  }

  @Test
  void testToStringMasksCardNumber() {
    BankPaymentRequest request = new BankPaymentRequest(
        "4111111111111111",
        "12/25",
        "USD",
        1000,
        "123"
    );

    String toString = request.toString();

    assertTrue(toString.contains("****1111"));
    assertFalse(toString.contains("4111111111111111"));
    assertTrue(toString.contains("cvv='***'"));
    assertFalse(toString.contains("123"));
  }

  @Test
  void testToStringWithNullCardNumber() {
    BankPaymentRequest request = new BankPaymentRequest();
    request.setCardNumber(null);

    String toString = request.toString();

    assertTrue(toString.contains("****"));
  }

  @Test
  void testToStringWithShortCardNumber() {
    BankPaymentRequest request = new BankPaymentRequest();
    request.setCardNumber("123");

    String toString = request.toString();

    assertTrue(toString.contains("****"));
  }
}
