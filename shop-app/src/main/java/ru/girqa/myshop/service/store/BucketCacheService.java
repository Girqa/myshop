package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Bucket;

@Service
@RequiredArgsConstructor
public class BucketCacheService {

  private final ReactiveRedisTemplate<String, Bucket> redisTemplate;

  public Mono<Bucket> getByUserId(@NonNull Long userId) {
    return redisTemplate.opsForValue().get(getKey(userId));
  }

  public Mono<Bucket> save(@NonNull Bucket bucket) {
    return redisTemplate.opsForValue().set(getKey(bucket.getUserId()), bucket)
        .thenReturn(bucket);
  }

  public Mono<Void> delete(@NonNull Long userId) {
    return redisTemplate.opsForValue().delete(getKey(userId))
        .then();
  }

  private String getKey(@NonNull Long userId) {
    return "bucket:user:%d".formatted(userId);
  }
}
