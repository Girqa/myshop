package ru.girqa.myshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.girqa.myshop.model.domain.OrderProduct;

public interface OrderProductRepository extends ReactiveCrudRepository<OrderProduct, Long> {

  Flux<OrderProduct> findByOrderId(Long id);
}
