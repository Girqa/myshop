package ru.girqa.myshop.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.OrderService;

@Service
@RequiredArgsConstructor
public class OrderFacadeService {

  private final OrderService orderService;

  private final BucketService bucketService;

  @Transactional
  public Order createOrder(@NonNull Long userId) {
    Bucket bucket = bucketService.findOrCreateByUserId(userId);
    if (bucket.getProducts().isEmpty()) {
      throw new IllegalStateException(
          "Order can not be created for empty bucket"
      );
    }
    Order order = orderService.create(bucket);
    bucket.clear();
    return order;
  }

  @Transactional(readOnly = true)
  public Order findById(@NonNull Long orderId) {
    return orderService.findById(orderId);
  }

  @Transactional(readOnly = true)
  public OrdersHistory getHistory() {
    List<Order> orders = orderService.findAll();
    BigDecimal totalPrice = orders.stream()
        .map(Order::getPrice)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);

    return OrdersHistory.builder()
        .totalPrice(totalPrice)
        .orders(orders)
        .build();
  }

}
