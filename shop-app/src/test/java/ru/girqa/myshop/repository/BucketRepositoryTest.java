package ru.girqa.myshop.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.bucket.BucketProductAmount;
import ru.girqa.myshop.model.domain.product.Product;

class BucketRepositoryTest extends BaseIntegrationTest {

  @Autowired
  R2dbcEntityTemplate entityTemplate;

  @Autowired
  BucketRepository bucketRepository;

  List<Product> products;

  @BeforeEach
  void setUp() {
    Image image = entityTemplate.insert(Image.builder()
            .name("Im")
            .size(5L)
            .data("data!".getBytes(StandardCharsets.UTF_8))
            .build())
        .block();
    assertNotNull(image);

    Product p1 = entityTemplate.insert(Product.builder()
            .name("P1")
            .description("D1")
            .price(new BigDecimal("321.23"))
            .imageId(image.getId())
            .build())
        .block();
    assertNotNull(p1);

    Product p2 = entityTemplate.insert(Product.builder()
            .name("P2")
            .description("D2")
            .price(new BigDecimal("23.32"))
            .imageId(image.getId())
            .build())
        .block();
    assertNotNull(p2);

    products = List.of(p1, p2);
  }

  @AfterEach
  void tearDown() {
    entityTemplate.getDatabaseClient()
        .sql("""
            truncate table buckets cascade;
            truncate table buckets_products cascade;
            truncate table products cascade;
            truncate table images cascade;
            """).fetch().all().blockLast();
  }

  @Nested
  class InsertTests {

    @Test
    void shouldSaveNewBucket() {
      Bucket bucket = Bucket.builder()
          .userId(113L)
          .build();

      Bucket savedBucket = assertDoesNotThrow(
          () -> bucketRepository.save(bucket).block()
      );

      assertNotNull(savedBucket);
      assertNotNull(savedBucket.getId());
      assertEquals(113L, savedBucket.getUserId());
      assertEquals(BigDecimal.ZERO, savedBucket.getTotalPrice());
    }

    @Test
    void shouldSaveProductForBucket() {
      Bucket bucket = assertDoesNotThrow(
          () -> bucketRepository.save(Bucket.builder()
              .userId(332L)
              .build()).block()
      );
      assertNotNull(bucket);

      BucketProductAmount productAmount = BucketProductAmount.builder()
          .bucketId(bucket.getId())
          .productId(products.getFirst().getId())
          .amount(3)
          .product(products.getFirst())
          .build();
      BucketProductAmount dbAmount = assertDoesNotThrow(
          () -> bucketRepository.saveProduct(productAmount).block()
      );
      assertNotNull(dbAmount);
      assertThat(dbAmount)
          .usingRecursiveComparison()
          .isEqualTo(productAmount);
    }
  }

  @Nested
  class SearchTests {

    Bucket bucket;

    @BeforeEach
    void setUpBucket() {
      bucket = bucketRepository.save(Bucket.builder()
          .userId(5223L)
          .build()).block();
      assertNotNull(bucket);

      List<BucketProductAmount> bucketProducts = Flux.fromIterable(products)
          .flatMap(p -> bucketRepository.saveProduct(BucketProductAmount.builder()
              .bucketId(bucket.getId())
              .productId(p.getId())
              .product(p)
              .amount(3)
              .build()))
          .collectList()
          .block();

      bucket = bucket.toBuilder()
          .products(bucketProducts)
          .build();
    }

    @Test
    void shouldFindWithProductsByUserId() {
      StepVerifier.create(bucketRepository.findWithFilledProductsByUserId(bucket.getUserId()))
          .assertNext(found -> assertThat(found)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .isEqualTo(bucket))
          .verifyComplete();
    }

    @Test
    void shouldFindWithProductsById() {
      StepVerifier.create(bucketRepository.findWithFilledProductsByUserId(bucket.getUserId()))
          .assertNext(found -> assertThat(found)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .isEqualTo(bucket))
          .verifyComplete();
    }

    @Test
    void shouldFindProduct() {
      StepVerifier.create(bucketRepository.findProduct(
          bucket.getId(), bucket.getProducts().getLast().getProductId())
      ).assertNext(found -> assertThat(found)
          .usingRecursiveComparison()
          .ignoringFields("product")
          .isEqualTo(bucket.getProducts().getLast())
      ).verifyComplete();
    }
  }

  @Nested
  class UpdateTests {

    Bucket bucket;

    @BeforeEach
    void setUpBucket() {
      bucket = bucketRepository.save(Bucket.builder()
          .userId(39L)
          .build()).block();
      assertNotNull(bucket);

      List<BucketProductAmount> bucketProducts = Flux.fromIterable(products)
          .flatMap(p -> bucketRepository.saveProduct(BucketProductAmount.builder()
              .bucketId(bucket.getId())
              .productId(p.getId())
              .product(p)
              .amount(12)
              .build()))
          .collectList()
          .block();

      bucket = bucket.toBuilder()
          .products(bucketProducts)
          .build();
    }

    @Test
    void shouldUpdateProductAmount() {
      BucketProductAmount updatedProduct = bucket.getProducts().getFirst()
          .toBuilder().amount(99)
          .build();

      StepVerifier.create(bucketRepository.updateProduct(updatedProduct))
          .assertNext(count -> assertEquals(1, count))
          .verifyComplete();

      StepVerifier.create(bucketRepository.findProduct(
              updatedProduct.getBucketId(), updatedProduct.getProductId())
          ).assertNext(found -> assertThat(found)
              .usingRecursiveComparison()
              .ignoringFields("product")
              .isEqualTo(updatedProduct))
          .verifyComplete();
    }

    @Test
    void shouldDeleteProduct() {
      BucketProductAmount toDelete = bucket.getProducts().getFirst();
      StepVerifier.create(bucketRepository.deleteProduct(
              toDelete.getBucketId(), toDelete.getProductId())
          ).assertNext(count -> assertEquals(1, count))
          .verifyComplete();

      StepVerifier.create(bucketRepository.findWithFilledProductsByUserId(bucket.getUserId()))
          .assertNext(found -> assertThat(found.getProducts())
              .hasSize(1)
              .first()
              .usingRecursiveComparison()
              .isEqualTo(bucket.getProducts().getLast())
          ).verifyComplete();
    }

    @Test
    void shouldDeleteAllBucketProducts() {
      StepVerifier.create(bucketRepository.deleteProductsByBucketId(bucket.getId()))
          .verifyComplete();

      StepVerifier.create(bucketRepository.findWithFilledProductsByUserId(bucket.getUserId()))
          .assertNext(found -> assertAll(
              () -> assertTrue(found.getProducts().isEmpty()),
              () -> assertEquals(BigDecimal.ZERO, found.getTotalPrice())
          )).verifyComplete();
    }
  }

}