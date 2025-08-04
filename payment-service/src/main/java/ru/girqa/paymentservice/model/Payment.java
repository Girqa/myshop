package ru.girqa.paymentservice.model;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class Payment {

  private Long userId;
  private Long orderId;
  private BigDecimal amount;

  public Payment(Long userId, Long orderId, BigDecimal amount) {
    this.userId = userId;
    this.orderId = orderId;
    this.amount = amount;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
