package ru.girqa.myshop.service;

import java.math.BigDecimal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openapitools.client.api.DefaultApi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import ru.girqa.myshop.exception.PaymentException;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.OrderService;
import ru.girqa.payment.dto.PaymentDto;

@Service
@RequiredArgsConstructor
public class OrderFacadeService {

  @Delegate
  private final OrderService orderService;

  private final BucketService bucketService;

  private final DefaultApi paymentGateway;

  @Transactional
  public Mono<Order> createOrder(@NonNull Long userId) {
    return bucketService.findFilledByUserId(userId)
        .flatMap(bucket -> {
          if (bucket.getProducts().isEmpty()) {
            return Mono.error(new IllegalStateException(
                "Order can not be created for empty bucket"
            ));
          }
          Mono<Order> createdOrder = orderService.create(bucket).cache();
          return createdOrder
              .flatMap(order -> {
                PaymentDto paymentDto = new PaymentDto();
                paymentDto.setOrderId(order.getId());
                paymentDto.setAmount(order.getPrice());
                return paymentGateway.doPaymentWithHttpInfo(userId, paymentDto)
                    .map(response -> Tuples.of(order, response));
              })
              .flatMap(orderAndResponse -> {
                Order order = orderAndResponse.getT1();
                ResponseEntity<String> response = orderAndResponse.getT2();
                if (response.getStatusCode().is2xxSuccessful()) {
                  return bucketService.clear(bucket.getId()).thenReturn(order);
                } else {
                  return orderService.deleteById(order.getId())
                      .then(Mono.error(new PaymentException(response.getBody())));
                }
              });
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
