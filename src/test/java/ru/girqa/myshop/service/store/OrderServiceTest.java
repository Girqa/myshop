package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductAmount;
import ru.girqa.myshop.repository.OrderRepository;

class OrderServiceTest extends BaseIntegrationTest {

  @Autowired
  OrderService orderService;

  @Autowired
  OrderRepository orderRepository;

  Order dbOrder;

  @BeforeEach
  void setUpOrder() {
    dbOrder = Order.builder()
        .price(new BigDecimal("234.21"))
        .products(List.of(
            OrderProduct.builder()
                .name("Op1")
                .price(new BigDecimal("124.11"))
                .amount(1)
                .description("Op1 desc")
                .image(Image.builder()
                    .name("Im1")
                    .size(32L)
                    .data("HEEEEEL".getBytes(StandardCharsets.UTF_8))
                    .build())
                .build(),
            OrderProduct.builder()
                .name("Op2")
                .price(new BigDecimal("55.05"))
                .amount(2)
                .description("Op2 desc")
                .image(Image.builder()
                    .name("Im2")
                    .size(23L)
                    .data("DOWN".getBytes(StandardCharsets.UTF_8))
                    .build())
                .build()
        ))
        .build();

    dbOrder.getProducts().forEach(p -> p.setOrder(dbOrder));

    dbOrder = orderRepository.save(dbOrder);
  }

  @AfterEach
  void tearDownOrder() {
    orderRepository.deleteAll();
  }

  @Nested
  @Transactional
  class SearchTests {

    @Test
    void shouldFindOneOrder() {
      List<Order> all = orderService.findAll();
      assertEquals(1, all.size());
      assertThat(all.getFirst())
          .usingRecursiveComparison()
          .isEqualTo(dbOrder);
    }

    @Test
    void shouldFindById() {
      Order order = orderService.findById(dbOrder.getId());
      assertThat(order)
          .usingRecursiveComparison()
          .isEqualTo(dbOrder);
    }

    @Test
    void shouldThrowNotFoundException() {
      assertThrows(
          ShopEntityNotFoundException.class,
          () -> orderService.findById(111111111L)
      );
    }
  }

  @Nested
  @Transactional
  class CreateTests {

    @Test
    void shouldCreateOrder() {
      Bucket bucket = Bucket.builder()
          .userId(51L)
          .products(List.of(
              ProductAmount.builder()
                  .amount(3)
                  .product(Product.builder()
                      .name("p1 name")
                      .price(new BigDecimal("32.2"))
                      .description("p1 desc")
                      .image(Image.builder()
                          .name("im 1")
                          .size(23L)
                          .data("Some image data".getBytes(StandardCharsets.UTF_8))
                          .build())
                      .build())
                  .build(),
              ProductAmount.builder()
                  .amount(4)
                  .product(Product.builder()
                      .name("p2 name")
                      .price(new BigDecimal("56.31"))
                      .description("p2 desc")
                      .image(Image.builder()
                          .name("im 2")
                          .size(165L)
                          .data("Tarabarshina".getBytes(StandardCharsets.UTF_8))
                          .build())
                      .build())
                  .build()
          )).build();

      Order order = assertDoesNotThrow(() -> orderService.create(bucket));

      assertNotNull(order.getCreatedAt());
      assertEquals(bucket.getTotalPrice(), order.getPrice());
      assertEquals(bucket.getProducts().size(), order.getProducts().size());

      for (int i = 0; i < order.getProducts().size(); ++i) {
        ProductAmount productAmount = bucket.getProducts().get(i);
        Product product = productAmount.getProduct();
        OrderProduct orderProduct = order.getProducts().get(i);

        assertEquals(productAmount.getAmount(), orderProduct.getAmount());
        assertEquals(product.getName(), orderProduct.getName());
        assertEquals(product.getPrice(), orderProduct.getPrice());
        assertEquals(product.getDescription(), orderProduct.getDescription());
        assertEquals(product.getImage(), orderProduct.getImage());
        assertEquals(order, orderProduct.getOrder());
      }
    }

    @Test
    void shouldThrowIllegalStateException() {
      Bucket emptyBucket = Bucket.builder()
          .products(List.of())
          .build();

      assertThrows(
          IllegalStateException.class,
          () -> orderService.create(emptyBucket)
      );
    }

  }

}