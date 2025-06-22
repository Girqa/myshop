package ru.girqa.myshop.repository;

import org.springframework.data.repository.CrudRepository;
import ru.girqa.myshop.model.domain.Order;

public interface OrderRepository extends CrudRepository<Order, Long> {

}
