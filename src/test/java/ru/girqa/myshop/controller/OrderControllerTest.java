package ru.girqa.myshop.controller;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.model.mapper.OrderMapper;
import ru.girqa.myshop.service.OrderFacadeService;

@WebMvcTest(controllers = OrderController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = OrderMapper.class
    ))
class OrderControllerTest {

  @Autowired
  MockMvc mvc;

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
        .thenReturn(createdOrder);

    mvc.perform(post("/order"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlTemplate("/order/{id}", createdOrder.getId()));
  }

  @Test
  @SneakyThrows
  void shouldGetOrderView() {
    final Order order = getOrder();
    when(orderFacadeServiceMock.findById(order.getId()))
        .thenReturn(order);

    mvc.perform(get("/order/{id}", order.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("order/order"))
        .andExpect(xpath("//h3").string(containsString(order.getId().toString())))
        .andExpect(xpath("//h3")
            .string(containsString(order.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))))
        .andExpect(xpath("//h4").string(containsString(order.getPrice().toString())))
        .andExpect(xpath("//article[contains(@class, 'col')]")
            .nodeCount(order.getProducts().size()))
        .andExpect(validProducts(order.getProducts()));
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
        .thenReturn(OrdersHistory.builder()
            .totalPrice(totalPrice)
            .orders(orders)
            .build());

    mvc.perform(get("/order/all"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("order/orders"))
        .andExpect(xpath("//h4").string(containsString(totalPrice.toString())))
        .andExpect(validOrders(orders));
  }

  private ResultMatcher validProducts(List<OrderProduct> products) {
    return result -> {
      for (int i = 0; i < products.size(); ++i) {
        OrderProduct product = products.get(i);
        String basePath = "//article[contains(@class, 'col')][%d]".formatted(i + 1);

        xpath(basePath + "/h5").string(containsString(product.getName()))
            .match(result);
        xpath(basePath + "/img[@src='/api/v1/image/%d']".formatted(product.getImage().getId()))
            .exists().match(result);
        xpath(basePath + "/div/p[1]").string(containsString(product.getPrice().toString()))
            .match(result);
        xpath(basePath + "/div/p[2]").string(containsString(String.valueOf(product.getAmount())))
            .match(result);
      }
    };
  }

  private ResultMatcher validOrders(List<Order> orders) {
    return result -> {
      xpath("//tr[@href]").nodeCount(orders.size())
          .match(result);
      for (Order order : orders) {
        String basePath = "//tbody/tr[@href='/order/%d']".formatted(order.getId());

        xpath(basePath).exists()
            .match(result);
        xpath(basePath + "/td[1]").string(containsString(order.getCreatedAt().toString()))
            .match(result);
        xpath(basePath + "/td[2]").string(containsString(order.getPrice().toString()))
            .match(result);
      }
    };
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
                .image(im1)
                .build(),
            OrderProduct.builder()
                .name("OP2")
                .description("OP2 Desc")
                .amount(random.nextInt())
                .price(BigDecimal.valueOf(random.nextDouble()))
                .image(im2)
                .build()
        ))
        .build();

    order.setId(random.nextLong());
    return order;
  }

}