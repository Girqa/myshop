package ru.girqa.myshop.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.girqa.myshop.model.domain.Bucket;

public interface BucketRepository extends CrudRepository<Bucket, Long> {

  @Query("""
         select b from Bucket b
         where b.userId = :userId
         """)
  Optional<Bucket> findByUserId(@Param("userId") Long userId);

}
