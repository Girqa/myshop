package ru.girqa.myshop.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.girqa.myshop.common.PostgresTestConfiguration;
import ru.girqa.myshop.model.domain.Bucket;

@DataJpaTest
@Import(PostgresTestConfiguration.class)
class BucketRepositoryTest {

  @Autowired
  BucketRepository bucketRepository;

  @Test
  void shouldFindBucketByUserId() {
    final Long USER_ID = 123L;

    Bucket bucket = bucketRepository.save(Bucket.builder()
        .userId(USER_ID)
        .products(List.of())
        .build());

    Optional<Bucket> dbBucket = assertDoesNotThrow(() -> bucketRepository.findByUserId(USER_ID));
    assertTrue(dbBucket.isPresent());

    assertAll(
        () -> assertEquals(bucket.getId(), dbBucket.get().getId()),
        () -> assertEquals(USER_ID, dbBucket.get().getUserId())
    );
  }
}