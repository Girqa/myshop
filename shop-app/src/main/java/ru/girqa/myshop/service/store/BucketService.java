package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

  private final BucketCacheService cacheService;

  @Transactional
  public Mono<Bucket> findFilledOrCreateByUserId(@NonNull Long userId) {
    return cacheService.getByUserId(userId)
        .switchIfEmpty(bucketRepository.findWithFilledProductsByUserId(userId)
            .switchIfEmpty(create(userId))
            .flatMap(cacheService::save)
        );
  }

  @Transactional(readOnly = true)
  public Mono<Bucket> findFilledByUserId(@NonNull Long userId) {
    return cacheService.getByUserId(userId)
        .switchIfEmpty(
            bucketRepository.findWithFilledProductsByUserId(userId)
                .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
                .flatMap(cacheService::save)
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
        .flatMap(cacheService::delete)
        .then();
  }

  @Transactional
  public Mono<Void> removeProduct(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.deleteProduct(bucketId, productId)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(cacheService::delete)
        .then();
  }

  @Transactional
  public Mono<Void> incrementProductCount(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.findProduct(bucketId, productId)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
        .doOnNext(BucketProductAmount::increment)
        .flatMap(bucketRepository::updateProduct)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(cacheService::delete)
        .then();
  }

  @Transactional
  public Mono<Void> decrementProductCount(@NonNull Long bucketId, @NonNull Long productId) {
    return bucketRepository.findProduct(bucketId, productId)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new))
        .doOnNext(BucketProductAmount::decrement)
        .flatMap(bucketRepository::updateProduct)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(cacheService::delete)
        .then();
  }

  @Transactional
  public Mono<Void> clear(@NonNull Long bucketId) {
    return bucketRepository.deleteProductsByBucketId(bucketId)
        .then(bucketRepository.getUserIdByBucketId(bucketId))
        .flatMap(cacheService::delete)
        .then();
  }
}
