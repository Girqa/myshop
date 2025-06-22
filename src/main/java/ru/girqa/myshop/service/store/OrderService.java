package ru.girqa.myshop.service.store;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.mapper.OrderProductMapper;
import ru.girqa.myshop.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;

  private final OrderProductMapper orderProductMapper;

  @Transactional(readOnly = true)
  public List<Order> findAll() {
    return StreamSupport
        .stream(orderRepository.findAll().spliterator(), false)
        .toList();
  }

  @Transactional(readOnly = true)
  public Order findById(@NonNull Long id) {
    return orderRepository.findById(id)
        .orElseThrow(ShopEntityNotFoundException::new);
  }

  @Transactional
  public Order create(@NonNull Bucket bucket) {
    List<OrderProduct> products = orderProductMapper.toOrder(bucket.getProducts());
    BigDecimal price = products.stream()
        .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getAmount())))
        .reduce(BigDecimal::add)
        .orElseThrow(() -> new IllegalStateException("Can not count order price"));

    Order order = Order.builder()
        .price(price)
        .products(products)
        .build();

    return orderRepository.save(order);
  }

}
