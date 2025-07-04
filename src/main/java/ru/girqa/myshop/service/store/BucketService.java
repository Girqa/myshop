package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.repository.BucketRepository;
import ru.girqa.myshop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class BucketService {

  private final BucketRepository bucketRepository;

  private final ProductRepository productRepository;

  @Transactional
  public Bucket findOrCreateByUserId(@NonNull Long userId) {
    return bucketRepository.findByUserId(userId)
        .orElseGet(() -> create(userId));
  }

  @Transactional(readOnly = true)
  public Bucket findById(@NonNull Long id) {
    return bucketRepository.findById(id)
        .orElseThrow(ShopEntityNotFoundException::new);
  }

  @Transactional
  public Bucket create(@NonNull Long userId) {
    return bucketRepository.save(Bucket.builder()
        .userId(userId)
        .build());
  }

  @Transactional
  public void addProduct(@NonNull Long bucketId, @NonNull Long productId) {
    Bucket bucket = findById(bucketId);
    Product product = productRepository.findById(productId)
        .orElseThrow(ShopEntityNotFoundException::new);
    bucket.addProduct(product);
  }

  @Transactional
  public void removeProduct(@NonNull Long bucketId, @NonNull Long productId) {
    Bucket bucket = findById(bucketId);
    bucket.removeProduct(productId);
  }

  @Transactional
  public void incrementProductCount(@NonNull Long bucketId, @NonNull Long productId) {
    Bucket bucket = findById(bucketId);
    bucket.increaseProduct(productId);
  }

  @Transactional
  public void decrementProductCount(@NonNull Long bucketId, @NonNull Long productId) {
    Bucket bucket = findById(bucketId);
    bucket.decreaseProduct(productId);
  }
}
