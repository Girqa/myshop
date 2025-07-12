package ru.girqa.myshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.girqa.myshop.model.domain.Image;

public interface ImageRepository extends ReactiveCrudRepository<Image, Long> {

}
