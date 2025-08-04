package ru.girqa.myshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.girqa.myshop.model.domain.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}
