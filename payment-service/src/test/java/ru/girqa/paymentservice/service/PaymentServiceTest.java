package ru.girqa.paymentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

  PaymentService paymentService = new PaymentService();

  @Test
  void shouldGetDefaultBalance() {
    assertEquals(PaymentService.DEFAULT_AMOUNT, paymentService.getBalance(123L).block());
  }

  @Test
  void shouldChangeBalance() {
    Long USER_ID = 2L;
    BigDecimal PURCHASE = new BigDecimal("723.55");
    paymentService.doPayment(USER_ID, 3L, PURCHASE).block();
    assertEquals(
        PaymentService.DEFAULT_AMOUNT.subtract(PURCHASE),
        paymentService.getBalance(USER_ID).block());
  }
}