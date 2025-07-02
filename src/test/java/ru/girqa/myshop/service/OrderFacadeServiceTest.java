package ru.girqa.myshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductAmount;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderFacadeServiceTest {

  @Mock
  OrderService orderServiceMock;

  @Mock
  BucketService bucketServiceMock;

  @InjectMocks
  OrderFacadeService orderFacadeService;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(orderServiceMock, bucketServiceMock);
  }

  @Nested
  class CreateTests {

    final Long USER_ID = 15L;

    @Test
    void shouldCreateOrder() {
      Bucket bucket = Bucket.builder()
          .userId(USER_ID)
          .products(new ArrayList<>(List.of(
              ProductAmount.builder()
                  .amount(2)
                  .product(Product.builder()
                      .price(new BigDecimal("15.3"))
                      .build())
                  .build(),
              ProductAmount.builder()
                  .amount(5)
                  .product(Product.builder()
                      .price(new BigDecimal("31.24"))
                      .build())
                  .build())))
          .build();

      bucket = spy(bucket);

      when(bucketServiceMock.findOrCreateByUserId(USER_ID))
          .thenReturn(bucket);

      Order givenOrder = Order.builder().build();
      when(orderServiceMock.create(bucket))
          .thenReturn(givenOrder);

      Order order = orderFacadeService.createOrder(USER_ID);
      assertThat(order).isSameAs(givenOrder);

      verify(bucket, times(1))
          .clear();
    }

    @Test
    void shouldThrowIllegalStateException() {
      Bucket bucket = Bucket.builder()
          .products(List.of())
          .build();

      when(bucketServiceMock.findOrCreateByUserId(USER_ID))
          .thenReturn(bucket);

      assertThrows(
          IllegalStateException.class,
          () -> orderFacadeService.createOrder(USER_ID)
      );
    }
  }

  @Nested
  class SearchTest {

    @Test
    void shouldReturnOrdersHistoryWithPrice() {
      List<Order> orders = List.of(
          Order.builder().price(new BigDecimal("54.4")).build(),
          Order.builder().price(new BigDecimal("31.2")).build(),
          Order.builder().price(new BigDecimal("959.5")).build()
      );

      when(orderServiceMock.findAll())
          .thenReturn(orders);

      OrdersHistory history = orderFacadeService.getHistory();
      assertEquals(orders, history.getOrders());

      BigDecimal totalPrice = orders.stream()
          .map(Order::getPrice)
          .reduce(BigDecimal::add)
          .orElseThrow();
      assertEquals(totalPrice, history.getTotalPrice());
    }

    @Test
    void shouldReturnEmptyHistory() {
      when(orderServiceMock.findAll())
          .thenReturn(List.of());

      OrdersHistory history = orderFacadeService.getHistory();
      assertTrue(history.getOrders().isEmpty());
      assertEquals(BigDecimal.ZERO, history.getTotalPrice());
    }

  }
}