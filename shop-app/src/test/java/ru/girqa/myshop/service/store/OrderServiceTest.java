package ru.girqa.myshop.service.store;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.bucket.BucketProductAmount;
import ru.girqa.myshop.model.domain.order.Order;
import ru.girqa.myshop.model.domain.order.OrderProduct;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.mapper.OrderProductMapper;
import ru.girqa.myshop.model.mapper.OrderProductMapperImpl;
import ru.girqa.myshop.repository.OrderProductRepository;
import ru.girqa.myshop.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  OrderRepository orderRepositoryMock;
  @Mock
  OrderProductRepository orderProductRepositoryMock;
  @Spy
  OrderProductMapper mapperMock = new OrderProductMapperImpl();
  @InjectMocks
  OrderService orderService;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(orderProductRepositoryMock, orderRepositoryMock, mapperMock);
  }

  @Nested
  class SearchTests {

    @Test
    void shouldFindAllOrdersWithoutProducts() {
      List<Order> orders = List.of(
          getOrder(5L, new BigDecimal("824.83")),
          getOrder(2L, new BigDecimal("38.11")),
          getOrder(91L, new BigDecimal("5.21"))
      );
      when(orderRepositoryMock.findAll())
          .thenReturn(Flux.fromIterable(orders));

      StepVerifier.create(orderService.findAll())
          .expectNextSequence(orders)
          .verifyComplete();
    }

    @Test
    void shouldFindOrderWithProducts() {
      Long orderId = 7L;
      List<OrderProduct> products = IntStream.range(0, 10)
          .mapToObj(v -> getProduct(orderId))
          .toList();
      BigDecimal price = products.stream()
          .map(OrderProduct::getPrice)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      Order expectedOrder = getOrder(orderId, price).toBuilder()
          .products(products)
          .build();

      when(orderRepositoryMock.findById(orderId))
          .thenReturn(Mono.just(expectedOrder));
      when(orderProductRepositoryMock.findByOrderId(orderId))
          .thenReturn(Flux.fromIterable(products));

      StepVerifier.create(orderService.findByIdWithProducts(orderId))
          .assertNext(found -> assertThat(found)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .ignoringFields("order")
              .isEqualTo(expectedOrder))
          .verifyComplete();
    }

    private Order getOrder(Long id, BigDecimal price) {
      return Order.builder()
          .id(id)
          .price(price)
          .createdAt(LocalDateTime.now())
          .build();
    }

    private OrderProduct getProduct(Long orderId) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      return OrderProduct.builder()
          .orderId(orderId)
          .amount(random.nextInt(1, 11))
          .imageId(random.nextLong(5, 8))
          .price(BigDecimal.valueOf(random.nextDouble(1.2, 44221.3)))
          .name("Name")
          .description("Desc")
          .build();
    }

  }

  @Nested
  class CreateTests {

    @Test
    void shouldCreateFromBucket() {
      Bucket bucket = Bucket.builder()
          .id(5L)
          .userId(21L)
          .products(List.of(BucketProductAmount.builder()
              .bucketId(5L)
              .productId(11L)
              .amount(3)
              .product(Product.builder()
                  .id(11L)
                  .imageId(2L)
                  .price(new BigDecimal("91.11"))
                  .description("D")
                  .name("N")
                  .build())
              .build()))
          .build();

      Order expectedOrder = Order.builder()
          .id(12L)
          .price(new BigDecimal("91.11").multiply(BigDecimal.valueOf(3)))
          .products(List.of(
              OrderProduct.builder()
                  .name("N")
                  .description("D")
                  .price(new BigDecimal("91.11"))
                  .imageId(2L)
                  .amount(3)
                  .orderId(12L)
                  .build()
          ))
          .build();

      when(orderRepositoryMock.save(any()))
          .then(call -> Mono.just(((Order) call.getArguments()[0]).toBuilder()
              .id(12L)
              .build()));
      when(orderProductRepositoryMock.saveAll(anyCollection()))
          .then(call -> Flux.fromIterable((List<?>) call.getArguments()[0]));

      StepVerifier.create(orderService.create(bucket))
          .assertNext(saved -> assertThat(saved)
              .usingRecursiveComparison()
              .ignoringFields("createdAt")
              .isEqualTo(expectedOrder)
          ).verifyComplete();
    }
  }
}