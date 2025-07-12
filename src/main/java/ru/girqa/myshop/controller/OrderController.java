package ru.girqa.myshop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.mapper.OrderMapper;
import ru.girqa.myshop.service.OrderFacadeService;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderFacadeService orderFacadeService;

  private final OrderMapper orderMapper;

  @PostMapping
  public Mono<String> createOrder(
      @RequestParam(name = "user_id", required = false, defaultValue = "1") Long userId) {
    return orderFacadeService.createOrder(userId)
        .map(order -> "redirect:/order/%d".formatted(order.getId()));
  }

  @GetMapping("/{id}")
  public Mono<Rendering> getOrder(@PathVariable Long id) {
    return orderFacadeService.findById(id)
        .map(orderMapper::toDto)
        .map(order -> Rendering.view("order/order")
            .modelAttribute("order", order)
            .build()
        );
  }

  @GetMapping("/all")
  public Mono<Rendering> getAllOrders() {
    return orderFacadeService.getHistory()
        .map(history -> Rendering.view("order/orders")
            .modelAttribute("totalPrice", history.getTotalPrice())
            .modelAttribute("orders", history.getOrders().stream()
                .map(orderMapper::toPreview)
                .toList()
            ).build()
        );
  }

}
