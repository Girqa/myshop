package ru.girqa.myshop.controller;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.service.OrderFacadeService;

class OrderControllerTest extends BaseIntegrationTest {

  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  OrderFacadeService orderFacadeServiceMock;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(orderFacadeServiceMock);
  }

  @Test
  @SneakyThrows
  void shouldCreateOrder() {
    final Order createdOrder = new Order();
    createdOrder.setId(5L);
    when(orderFacadeServiceMock.createOrder(anyLong()))
        .thenReturn(Mono.just(createdOrder));

    webTestClient.post()
        .uri("/order")
        .exchange()
        .expectStatus().is3xxRedirection()
        .expectHeader().location("/order/5");
  }

  @Test
  @SneakyThrows
  void shouldGetOrderView() {
    final Order order = getOrder();
    when(orderFacadeServiceMock.findByIdWithProducts(order.getId()))
        .thenReturn(Mono.just(order));

    webTestClient.get()
        .uri("/order/{id}", order.getId())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.TEXT_HTML)
        .expectBody()
        .xpath("//h3").string(containsString(order.getId().toString()))
        .xpath("//h3")
        .string(containsString(order.getCreatedAt()
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))))
        .xpath("//h4").string(containsString(order.getPrice().toString()))
        .xpath("//article[contains(@class, 'col')]")
        .nodeCount(order.getProducts().size());
  }

  @Test
  @SneakyThrows
  void shouldGetAllOrdersView() {
    List<Order> orders = IntStream.range(0, 5)
        .mapToObj(v -> getOrder())
        .toList();
    BigDecimal totalPrice = orders.stream()
        .map(Order::getPrice)
        .reduce(BigDecimal::add)
        .orElseThrow();

    when(orderFacadeServiceMock.getHistory())
        .thenReturn(Mono.just(OrdersHistory.builder()
            .totalPrice(totalPrice)
            .orders(orders)
            .build()));

    BodyContentSpec spec = webTestClient.get()
        .uri("/order/all")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.TEXT_HTML)
        .expectBody()
        .xpath("//h4").string(containsString(totalPrice.toString()))
        .xpath("//tr[@href]").nodeCount(orders.size());

    for (Order order : orders) {
      String basePath = "//tbody/tr[@href='/order/%d']".formatted(order.getId());

      spec = spec.xpath(basePath).exists();
      spec = spec.xpath(basePath + "/td[1]")
          .string(containsString(order.getCreatedAt().toString()));
      spec = spec.xpath(basePath + "/td[2]").string(containsString(order.getPrice().toString()));
    }
  }

  private Order getOrder() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Image im1 = new Image();
    im1.setId(random.nextLong());

    Image im2 = new Image();
    im2.setId(random.nextLong());

    Order order = Order.builder()
        .price(BigDecimal.valueOf(random.nextDouble()))
        .createdAt(LocalDateTime.now())
        .products(List.of(
            OrderProduct.builder()
                .name("OP1")
                .description("OP1 Desc")
                .amount(random.nextInt())
                .price(BigDecimal.valueOf(random.nextDouble()))
                .imageId(im1.getId())
                .build(),
            OrderProduct.builder()
                .name("OP2")
                .description("OP2 Desc")
                .amount(random.nextInt())
                .price(BigDecimal.valueOf(random.nextDouble()))
                .imageId(im2.getId())
                .build()
        ))
        .build();

    order.setId(random.nextLong());
    return order;
  }

}