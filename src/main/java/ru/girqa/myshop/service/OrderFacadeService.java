package ru.girqa.myshop.service;

import java.math.BigDecimal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.OrderService;

@Service
@RequiredArgsConstructor
public class OrderFacadeService {

  @Delegate
  private final OrderService orderService;

  private final BucketService bucketService;

  @Transactional
  public Mono<Order> createOrder(@NonNull Long userId) {
    return bucketService.findFilledById(userId)
        .flatMap(bucket -> {
          if (bucket.getProducts().isEmpty()) {
            return Mono.error(new IllegalStateException(
                "Order can not be created for empty bucket"
            ));
          }
          return orderService.create(bucket)
              .flatMap(order -> bucketService.clear(bucket.getId())
                  .thenReturn(order));
        });
  }

  @Transactional(readOnly = true)
  public Mono<OrdersHistory> getHistory() {
    Flux<Order> orders = orderService.findAll();

    Mono<BigDecimal> totalPrice = orders
        .map(Order::getPrice)
        .reduce(BigDecimal::add)
        .defaultIfEmpty(BigDecimal.ZERO);

    return Mono.zip(orders.collectList(), totalPrice)
        .map(t -> OrdersHistory.builder()
            .orders(t.getT1())
            .totalPrice(t.getT2())
            .build());
  }

}
