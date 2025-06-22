package ru.girqa.myshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.girqa.myshop.model.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
