package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Bucket;

class BucketCacheServiceTest extends BaseIntegrationTest {

  @Autowired
  BucketCacheService bucketCacheService;

  @Test
  void shouldSaveBucket() {
    final Long USER_ID = 11L;
    final Bucket bucket = Bucket.builder()
        .userId(USER_ID)
        .build();
    final Bucket savedBucket = bucketCacheService.save(bucket).block();

    assertNotNull(savedBucket);
    assertEquals(USER_ID, savedBucket.getUserId());

    Bucket cachedBucket = bucketCacheService.getByUserId(USER_ID).block();

    assertThat(cachedBucket)
        .usingRecursiveComparison()
        .isEqualTo(savedBucket);
  }

  @Test
  void shouldDeleteBucket() {
    final Long USER_ID = 32L;
    final Bucket bucket = Bucket.builder()
        .userId(USER_ID)
        .build();
    bucketCacheService.save(bucket).block();
    bucketCacheService.delete(USER_ID).block();
    assertNull(bucketCacheService.getByUserId(USER_ID).block());
  }

}