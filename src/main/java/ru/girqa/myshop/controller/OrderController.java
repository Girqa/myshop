package ru.girqa.myshop.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrdersHistory;
import ru.girqa.myshop.model.dto.order.OrderDto;
import ru.girqa.myshop.model.dto.order.OrderPreviewDto;
import ru.girqa.myshop.model.mapper.OrderMapper;
import ru.girqa.myshop.service.OrderFacadeService;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderFacadeService orderFacadeService;

  private final OrderMapper orderMapper;

  @PostMapping
  public String createOrder(
      @RequestParam(name = "user_id", required = false, defaultValue = "1") Long userId
  ) {
    Order order = orderFacadeService.createOrder(userId);
    return "redirect:/order/%d".formatted(order.getId());
  }

  @GetMapping("/{id}")
  public String getOrder(@PathVariable Long id, Model model) {
    Order order = orderFacadeService.findById(id);
    OrderDto dto = orderMapper.toDto(order);
    model.addAttribute("order", dto);
    return "order/order";
  }

  @GetMapping("/all")
  public String getAllOrders(Model model) {
    OrdersHistory history = orderFacadeService.getHistory();
    List<OrderPreviewDto> orders = history.getOrders().stream()
        .map(orderMapper::toPreview)
        .toList();

    model.addAttribute("totalPrice", history.getTotalPrice());
    model.addAttribute("orders", orders);

    return "order/orders";
  }

}
