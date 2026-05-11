package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @PostMapping("/payments")
  public ResponseEntity<PostPaymentResponse> processPayment(
      @Valid @RequestBody PostPaymentRequest request) {
    PostPaymentResponse response = paymentGatewayService.processPayment(request);

    // Return 201 for Authorized, 200 for Declined
    HttpStatus status = response.getStatus() == PaymentStatus.AUTHORIZED
        ? HttpStatus.CREATED
        : HttpStatus.OK;

    return new ResponseEntity<>(response, status);
  }

  @GetMapping("/payments/{id}")
  public ResponseEntity<GetPaymentResponse> getPaymentById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }
}
