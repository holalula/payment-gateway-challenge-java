package com.checkout.payment.gateway.model.bank;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BankPaymentResponseTest {

  @Test
  void testDefaultConstructor() {
    BankPaymentResponse response = new BankPaymentResponse();
    assertNotNull(response);
    assertFalse(response.isAuthorized());
  }

  @Test
  void testParameterizedConstructor() {
    BankPaymentResponse response = new BankPaymentResponse(true, "AUTH123");

    assertTrue(response.isAuthorized());
    assertEquals("AUTH123", response.getAuthorizationCode());
  }

  @Test
  void testSettersAndGetters() {
    BankPaymentResponse response = new BankPaymentResponse();

    response.setAuthorized(true);
    response.setAuthorizationCode("AUTH456");

    assertTrue(response.isAuthorized());
    assertEquals("AUTH456", response.getAuthorizationCode());
  }

  @Test
  void testToString() {
    BankPaymentResponse response = new BankPaymentResponse(false, "DECLINED789");

    String toString = response.toString();

    assertTrue(toString.contains("authorized=false"));
    assertTrue(toString.contains("authorizationCode='DECLINED789'"));
  }
}
