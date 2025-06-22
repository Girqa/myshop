package ru.girqa.myshop.repository;

import org.springframework.data.repository.CrudRepository;
import ru.girqa.myshop.model.domain.Bucket;

public interface BucketRepository extends CrudRepository<Bucket, Long> {

}
