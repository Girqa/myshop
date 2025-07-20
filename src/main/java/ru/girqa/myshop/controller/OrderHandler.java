package ru.girqa.myshop.controller;

import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.mapper.OrderMapper;
import ru.girqa.myshop.service.OrderFacadeService;

@Component
@RequiredArgsConstructor
public class OrderHandler {

  private final OrderFacadeService orderFacadeService;

  private final OrderMapper orderMapper;

  public Mono<ServerResponse> createOrder(ServerRequest request) {
    Long userId = request.queryParam("user_id")
        .map(Long::valueOf)
        .orElse(1L);

    return orderFacadeService.createOrder(userId)
        .map(Order::getId)
        .map(id -> URI.create("/order/%d".formatted(id)))
        .flatMap(uri -> ServerResponse.status(HttpStatus.FOUND)
            .header("Location", uri.getPath())
            .build());
  }

  public Mono<ServerResponse> getOrder(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));
    return orderFacadeService.findByIdWithProducts(id)
        .map(orderMapper::toDto)
        .flatMap(order -> ServerResponse.ok().render("order/order", Map.of(
            "order", order
        )));
  }

  public Mono<ServerResponse> getAllOrders(ServerRequest ignoredRequest) {
    return orderFacadeService.getHistory()
        .flatMap(history -> ServerResponse.ok().render("order/orders", Map.of(
            "totalPrice", history.getTotalPrice(),
            "orders", history.getOrders().stream()
                .map(orderMapper::toPreview)
                .toList()
        )));
  }

}
