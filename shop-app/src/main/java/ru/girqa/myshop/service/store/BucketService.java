package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.BucketProductAmount;
import ru.girqa.myshop.repository.BucketRepository;

@Service
@RequiredArgsConstructor
public class BucketService {

  private final BucketRepository bucketRepository;

  private final ReactiveRedisTemplate<String, Bucket> redisTemplate;

  @Transactional
  public Mono<Bucket> findFilledOrCreateByUserId(@NonNull Long userId) {
    String key = userId.toString();

    return redisTemplate.opsForValue().get(key)
        .switchIfEmpty(
            bucketRepository.findWithFilledProductsByUserId(userId)
                .switchIfEmpty(create(userId))
                .flatMap(bucket ->
                    redisTemplate.opsForValue().set(key, bucket)
                        .thenReturn(bucket)
                )
        );
  }

  @Transactional(readOnly = true)
  public Mono<Bucket> findFilledByUserId(@NonNull Long userId) {
    String key = userId.toString();

    return redisTemplate.opsForValue().get(key)
        .switchIfEmpty(
            bucketRepository.findWithFilledProductsByUserId(userId)
                .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
                .flatMap(bucket ->
                    redisTemplate.opsForValue().set(key, bucket)
                        .thenReturn(bucket)
                )
        );
  }

  @Transactional
  public Mono<Bucket> create(@NonNull Long userId) {
    return bucketRepository.save(Bucket.builder()
        .userId(userId)
        .build());
  }

  @Transactional
  public Mono<Void> addProduct(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.saveProduct(BucketProductAmount.builder()
            .bucketId(bucketId)
            .productId(productId)
            .amount(1)
            .build())
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(userId -> redisTemplate.opsForValue().delete(userId.toString()))
        .then();
  }

  @Transactional
  public Mono<Void> removeProduct(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.deleteProduct(bucketId, productId)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(userId -> redisTemplate.opsForValue().delete(userId.toString()))
        .then();
  }

  @Transactional
  public Mono<Void> incrementProductCount(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.findProduct(bucketId, productId)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
        .doOnNext(BucketProductAmount::increment)
        .flatMap(bucketRepository::updateProduct)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(userId -> redisTemplate.opsForValue().delete(userId.toString()))
        .then();
  }

  @Transactional
  public Mono<Void> decrementProductCount(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.findProduct(bucketId, productId)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
        .doOnNext(BucketProductAmount::decrement)
        .flatMap(bucketRepository::updateProduct)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(userId -> redisTemplate.opsForValue().delete(userId.toString()))
        .then();
  }

  @Transactional
  public Mono<Void> clear(@NonNull Long bucketId) {
    return bucketRepository.deleteProductsByBucketId(bucketId)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(userId -> redisTemplate.opsForValue().delete(userId.toString()))
        .then();
  }
}
