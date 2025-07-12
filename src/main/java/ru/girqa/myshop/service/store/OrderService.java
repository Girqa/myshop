package ru.girqa.myshop.service.store;

import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.mapper.OrderProductMapper;
import ru.girqa.myshop.repository.OrderProductRepository;
import ru.girqa.myshop.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;

  private final OrderProductRepository orderProductRepository;

  private final OrderProductMapper orderProductMapper;

  @Transactional(readOnly = true)
  public Flux<Order> findAll() {
    return orderRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Mono<Order> findById(@NonNull Long id) {
    return orderRepository.findById(id)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
        .flatMap(order -> orderProductRepository.findByOrderId(order.getId())
            .collectList()
            .map(products -> {
              order.setProducts(products);
              return order;
            })
        );
  }

  @Transactional
  public Mono<Order> create(@NonNull Bucket bucket) {
    Mono<List<OrderProduct>> products = Mono.fromCallable(bucket::getProducts)
        .map(orderProductMapper::toOrder);

    Mono<BigDecimal> price = products.flatMapMany(Flux::fromIterable)
        .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getAmount())))
        .reduce(BigDecimal::add)
        .switchIfEmpty(Mono.error(new IllegalStateException("Can not count order price")));

    Mono<Order> order = price
        .map(p -> Order.builder()
            .price(p)
            .build())
        .flatMap(orderRepository::save)
        .cache();

    products = products.flatMap(mappedProducts -> order.map(o -> {
          mappedProducts.forEach(p -> p.setOrderId(o.getId()));
          return mappedProducts;
        }))
        .flatMapMany(orderProductRepository::saveAll)
        .collectList();

    return products
        .flatMap(all -> order.map(o -> {
          o.setProducts(all);
          return o;
        }));
  }

}
