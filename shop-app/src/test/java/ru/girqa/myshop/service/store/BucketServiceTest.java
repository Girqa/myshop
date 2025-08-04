package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.BucketProductAmount;
import ru.girqa.myshop.repository.BucketRepository;

@ExtendWith(MockitoExtension.class)
class BucketServiceTest {

  @Mock
  BucketRepository bucketRepositoryMock;
  @Mock
  BucketCacheService bucketCacheServiceMock;
  @InjectMocks
  BucketService bucketService;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(bucketRepositoryMock, bucketCacheServiceMock);
  }

  @Nested
  class SearchTests {

    Bucket bucket = Bucket.builder()
        .id(3L)
        .userId(4L)
        .build();

    @Test
    void shouldSwitchToErrorIfNotFoundByUserId() {
      when(bucketRepositoryMock.findWithFilledProductsByUserId(anyLong()))
          .thenReturn(Mono.empty());
      when(bucketCacheServiceMock.getByUserId(anyLong()))
          .thenReturn(Mono.empty());

      StepVerifier.create(bucketService.findFilledByUserId(bucket.getId()))
          .verifyError(ShopEntityNotFoundException.class);

      verify(bucketRepositoryMock, times(1))
          .findWithFilledProductsByUserId(anyLong());
    }

    @Test
    void shouldFindByUserIdInDb() {
      when(bucketRepositoryMock.findWithFilledProductsByUserId(bucket.getUserId()))
          .thenReturn(Mono.just(bucket));
      when(bucketRepositoryMock.save(any()))
          .thenReturn(Mono.empty());
      when(bucketCacheServiceMock.getByUserId(bucket.getUserId()))
          .thenReturn(Mono.empty());
      when(bucketCacheServiceMock.save(any()))
          .thenReturn(Mono.just(bucket));

      StepVerifier.create(bucketService.findFilledOrCreateByUserId(bucket.getUserId()))
          .assertNext(found -> assertEquals(bucket, found))
          .verifyComplete();

      verify(bucketRepositoryMock, times(1))
          .findWithFilledProductsByUserId(anyLong());
      verify(bucketRepositoryMock, times(1))
          .save(any());
    }

  }

  @Nested
  class CreateTests {

    Bucket bucket = Bucket.builder()
        .id(52L)
        .userId(32L)
        .build();

    @Test
    void shouldCreate() {
      when(bucketRepositoryMock.save(any()))
          .thenReturn(Mono.just(bucket));

      StepVerifier.create(bucketService.create(bucket.getUserId()))
          .assertNext(created -> assertThat(created)
              .usingRecursiveComparison()
              .isEqualTo(bucket))
          .verifyComplete();

      verify(bucketRepositoryMock, times(1))
          .save(any());
    }

    @Test
    void shouldCreateByUserIfNotFound() {
      when(bucketRepositoryMock.findWithFilledProductsByUserId(bucket.getUserId()))
          .thenReturn(Mono.empty());
      when(bucketRepositoryMock.save(any()))
          .thenReturn(Mono.just(bucket));
      when(bucketCacheServiceMock.getByUserId(bucket.getUserId()))
          .thenReturn(Mono.empty());
      when(bucketCacheServiceMock.save(any()))
          .thenReturn(Mono.just(bucket));

      StepVerifier.create(bucketService.findFilledOrCreateByUserId(bucket.getUserId()))
          .assertNext(found -> assertEquals(bucket, found))
          .verifyComplete();

      verify(bucketRepositoryMock, times(1))
          .findWithFilledProductsByUserId(anyLong());
      verify(bucketRepositoryMock, times(1))
          .save(any());
    }

  }

  @Nested
  class UpdateTests {

    final Long BUCKET_ID = 91L;
    final Long PRODUCT_ID = 3L;

    @Test
    void shouldSaveProduct() {
      when(bucketRepositoryMock.saveProduct(any()))
          .thenReturn(Mono.empty());
      when(bucketRepositoryMock.getUserIdByBucketId(anyLong())).thenReturn(Mono.just(4L));
      when(bucketCacheServiceMock.delete(anyLong())).thenReturn(Mono.empty());

      bucketService.addProduct(BUCKET_ID, PRODUCT_ID).block();

      ArgumentCaptor<BucketProductAmount> captor = ArgumentCaptor.forClass(
          BucketProductAmount.class);
      verify(bucketRepositoryMock, times(1))
          .saveProduct(captor.capture());

      BucketProductAmount product = captor.getValue();
      assertNotNull(product);
      assertAll(
          () -> assertEquals(BUCKET_ID, product.getBucketId()),
          () -> assertEquals(PRODUCT_ID, product.getProductId()),
          () -> assertEquals(1, product.getAmount())
      );
    }

    @Test
    void shouldDeleteProduct() {
      when(bucketRepositoryMock.deleteProduct(anyLong(), anyLong()))
          .thenReturn(Mono.empty());
      when(bucketRepositoryMock.getUserIdByBucketId(anyLong()))
          .thenReturn(Mono.just(4L));
      when(bucketCacheServiceMock.delete(anyLong())).thenReturn(Mono.empty());

      bucketService.removeProduct(BUCKET_ID, PRODUCT_ID).block();

      verify(bucketRepositoryMock, times(1))
          .deleteProduct(BUCKET_ID, PRODUCT_ID);
      verify(bucketCacheServiceMock, times(1))
          .delete(4L);
    }

    @Test
    void shouldClearProducts() {
      when(bucketRepositoryMock.deleteProductsByBucketId(any())).thenReturn(Mono.empty());
      when(bucketRepositoryMock.getUserIdByBucketId(anyLong())).thenReturn(Mono.just(5L));
      when(bucketCacheServiceMock.delete(anyLong())).thenReturn(Mono.empty());

      bucketService.clear(BUCKET_ID).block();

      verify(bucketRepositoryMock, times(1))
          .deleteProductsByBucketId(BUCKET_ID);
      verify(bucketCacheServiceMock, times(1))
          .delete(5L);
    }

    @Test
    void shouldIncrementAmount() {
      when(bucketRepositoryMock.findProduct(anyLong(), anyLong()))
          .thenReturn(Mono.just(BucketProductAmount.builder()
              .amount(2)
              .bucketId(BUCKET_ID)
              .productId(PRODUCT_ID)
              .build()));
      when(bucketRepositoryMock.updateProduct(any())).thenReturn(Mono.empty());
      when(bucketRepositoryMock.getUserIdByBucketId(anyLong())).thenReturn(Mono.just(4L));
      when(bucketCacheServiceMock.delete(anyLong())).thenReturn(Mono.empty());

      bucketService.incrementProductCount(BUCKET_ID, PRODUCT_ID).block();

      ArgumentCaptor<BucketProductAmount> captor = ArgumentCaptor.forClass(
          BucketProductAmount.class);
      verify(bucketRepositoryMock, times(1))
          .findProduct(BUCKET_ID, PRODUCT_ID);
      verify(bucketRepositoryMock, times(1))
          .updateProduct(captor.capture());

      BucketProductAmount product = captor.getValue();
      assertNotNull(product);
      assertEquals(3, product.getAmount());
    }

    @Test
    void shouldDecrementAmount() {
      when(bucketRepositoryMock.findProduct(anyLong(), anyLong()))
          .thenReturn(Mono.just(BucketProductAmount.builder()
              .amount(2)
              .bucketId(BUCKET_ID)
              .productId(PRODUCT_ID)
              .build()));
      when(bucketRepositoryMock.updateProduct(any())).thenReturn(Mono.empty());
      when(bucketRepositoryMock.getUserIdByBucketId(anyLong()))
          .thenReturn(Mono.just(5L));
      when(bucketCacheServiceMock.delete(anyLong())).thenReturn(Mono.empty());

      bucketService.decrementProductCount(BUCKET_ID, PRODUCT_ID).block();

      ArgumentCaptor<BucketProductAmount> captor = ArgumentCaptor.forClass(
          BucketProductAmount.class);
      verify(bucketRepositoryMock, times(1))
          .findProduct(BUCKET_ID, PRODUCT_ID);
      verify(bucketRepositoryMock, times(1))
          .updateProduct(captor.capture());
      verify(bucketCacheServiceMock, times(1))
          .delete(5L);

      BucketProductAmount product = captor.getValue();
      assertNotNull(product);
      assertEquals(1, product.getAmount());
    }
  }

}