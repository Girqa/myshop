package ru.girqa.myshop.model.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrdersHistory {

  private BigDecimal totalPrice;

  private List<Order> orders;

}
