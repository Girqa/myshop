package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.repository.BucketRepository;
import ru.girqa.myshop.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class BucketServiceTest {

  @Mock
  BucketRepository bucketRepositoryMock;

  @Mock
  ProductRepository productRepositoryMock;

  @InjectMocks
  BucketService bucketService;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(bucketRepositoryMock, productRepositoryMock);
  }

  @Nested
  class SearchTests {

    final Long BUCKET_ID = 652L;
    final Long USER_ID = 123L;
    Bucket givenBucket;

    @BeforeEach
    void setUpBucket() {
      givenBucket = Bucket.builder()
          .userId(USER_ID)
          .products(List.of())
          .build();

      givenBucket.setId(BUCKET_ID);
    }

    @Test
    void shouldFindBucketById() {
      when(bucketRepositoryMock.findById(BUCKET_ID))
          .thenReturn(Optional.of(givenBucket));

      Bucket bucket = assertDoesNotThrow(() -> bucketService.findById(BUCKET_ID));
      assertThat(bucket)
          .usingRecursiveComparison()
          .isEqualTo(givenBucket);
    }

    @Test
    void shouldNotFindBucketById() {
      when(bucketRepositoryMock.findById(any()))
          .thenReturn(Optional.empty());

      assertThrows(
          ShopEntityNotFoundException.class,
          () -> bucketService.findById(BUCKET_ID)
      );
    }

    @Test
    void shouldFindBucketByUserId() {
      when(bucketRepositoryMock.findByUserId(USER_ID))
          .thenReturn(Optional.of(givenBucket));

      Bucket bucket = assertDoesNotThrow(() -> bucketService.findOrCreateByUserId(USER_ID));
      assertThat(bucket)
          .usingRecursiveComparison()
          .isEqualTo(givenBucket);
    }

    @Test
    void shouldCreateBucket() {
      final Bucket preparedBucket = Bucket.builder()
          .userId(USER_ID)
          .build();

      when(bucketRepositoryMock.findByUserId(any()))
          .thenReturn(Optional.empty());
      when(bucketRepositoryMock.save(any()))
          .thenReturn(preparedBucket);

      Bucket bucket = assertDoesNotThrow(() -> bucketService.findOrCreateByUserId(USER_ID));
      verify(bucketRepositoryMock, times(1))
          .save(any());
      assertThat(bucket)
          .usingRecursiveComparison()
          .isEqualTo(preparedBucket);
    }

  }

  @Nested
  class CreationTests {

    @Test
    void shouldSaveBucketFroUser() {
      final Long USER_ID = 5231L;

      bucketService.create(USER_ID);

      ArgumentCaptor<Bucket> captor = ArgumentCaptor.forClass(Bucket.class);
      verify(bucketRepositoryMock, times(1))
          .save(captor.capture());

      Bucket saved = captor.getValue();
      assertEquals(USER_ID, saved.getUserId());
    }
  }

  @Nested
  class UpdatingTests {
    final Long BUCKET_ID = 652L;
    final Long PRODUCT_ID = 11L;
    Bucket bucketMock;

    @BeforeEach
    void setUpBucket() {
      bucketMock = mock(Bucket.class);

      when(bucketRepositoryMock.findById(BUCKET_ID))
          .thenReturn(Optional.of(bucketMock));
    }

    @Test
    void shouldAddProduct() {
      Product givenProduct = Product.builder()
          .name("Product")
          .description("Description P")
          .price(new BigDecimal("99.22"))
          .build();

      when(productRepositoryMock.findById(PRODUCT_ID))
          .thenReturn(Optional.of(givenProduct));

      bucketService.addProduct(BUCKET_ID, PRODUCT_ID);

      verify(bucketMock, times(1))
          .addProduct(givenProduct);
    }

    @Test
    void shouldThrowOnNotPresentProductAdd() {
      when(productRepositoryMock.findById(any()))
          .thenReturn(Optional.empty());

      assertThrows(
          ShopEntityNotFoundException.class,
          () -> bucketService.addProduct(BUCKET_ID, PRODUCT_ID)
      );
    }

    @Test
    void shouldRemoveProduct() {
      bucketService.removeProduct(BUCKET_ID, PRODUCT_ID);
      verify(bucketMock, times(1))
          .removeProduct(PRODUCT_ID);
    }

    @Test
    void shouldIncrementProduct() {
      bucketService.incrementProductCount(BUCKET_ID, PRODUCT_ID);
      verify(bucketMock, times(1))
          .increaseProduct(PRODUCT_ID);
    }

    @Test
    void shouldDecrementProduct() {
      bucketService.decrementProductCount(BUCKET_ID, PRODUCT_ID);
      verify(bucketMock, times(1))
          .decreaseProduct(PRODUCT_ID);
    }
  }

}