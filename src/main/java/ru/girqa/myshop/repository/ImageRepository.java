package ru.girqa.myshop.repository;

import org.springframework.data.repository.CrudRepository;
import ru.girqa.myshop.model.domain.Image;

public interface ImageRepository extends CrudRepository<Image, Long> {

}
