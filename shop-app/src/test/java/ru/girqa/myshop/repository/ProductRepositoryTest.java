package ru.girqa.myshop.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;

@DirtiesContext
class ProductRepositoryTest extends BaseIntegrationTest {

  @Autowired
  ProductRepository productRepository;

  @Autowired
  R2dbcEntityTemplate entityTemplate;

  @AfterEach
  void tearDownDb() {
    entityTemplate.getDatabaseClient()
        .sql("""
            truncate table products cascade;
            truncate table images cascade;
            """).fetch().all().blockLast();
  }

  @Nested
  class SearchTests {

    List<Product> givenProducts;

    @BeforeEach
    void setUpProducts() {
      givenProducts = entityTemplate.insert(Image.builder()
              .name("im name")
              .size(10L)
              .data("Image data".getBytes())
              .build())
          .<Product>flatMapMany(image -> Flux.fromStream(
              IntStream.range(1, 4)
                  .mapToObj(i -> Product.builder()
                      .name("Name %d".formatted(i))
                      .imageId(image.getId())
                      .description("Desc %d".formatted(i))
                      .price(new BigDecimal("10.00").multiply(BigDecimal.valueOf(i)))
                      .build())
          ))
          .flatMap(entityTemplate::insert)
          .sort(Comparator.comparingLong(Product::getId))
          .collectList()
          .block();
    }

    @Test
    void shouldFindProductById() {
      Product expectedProduct = givenProducts.getFirst();

      StepVerifier.create(productRepository.findById(expectedProduct.getId()))
          .consumeNextWith(actualProduct -> assertThat(actualProduct)
              .isNotNull()
              .usingRecursiveComparison()
              .isEqualTo(expectedProduct))
          .verifyComplete();
    }

    @Test
    void shouldNotFindProduct() {
      final Long notPresentId = givenProducts.getLast().getId() + 5;

      StepVerifier.create(productRepository.findById(notPresentId))
          .expectNextCount(0L)
          .verifyComplete();
    }

    @Test
    void shouldFindAllProducts() {
      ProductPageRequest pageRequest = ProductPageRequest.builder()
          .page(0)
          .pageSize(givenProducts.size())
          .build();

      StepVerifier.create(productRepository.findAll(pageRequest))
          .consumeNextWith(page -> assertAll(
              () -> assertEquals(1, page.getTotalPages()),
              () -> assertEquals(givenProducts.size(), page.getProducts().size()),
              () -> assertThat(page.getProducts())
                  .usingRecursiveComparison()
                  .ignoringCollectionOrder()
                  .isEqualTo(givenProducts))
          ).verifyComplete();
    }

    @Test
    void shouldFindOneMatchingProductByName() {
      ProductPageRequest pageRequest = ProductPageRequest.builder()
          .page(0)
          .pageSize(givenProducts.size())
          .build();

      StepVerifier.create(productRepository.findAllByName(
          givenProducts.getFirst().getName(), pageRequest)
      ).consumeNextWith(page -> assertAll(
          () -> assertEquals(1, page.getTotalPages()),
          () -> assertEquals(1, page.getProducts().size()),
          () -> assertThat(page.getProducts().getFirst())
              .usingRecursiveComparison()
              .isEqualTo(givenProducts.getFirst()))
      ).verifyComplete();
    }
  }

  @Nested
  class InsertionTests {

    Image image;

    @BeforeEach
    void setUpImage() {
      image = entityTemplate.insert(Image.builder()
              .name("Test image")
              .data("Hello image".getBytes())
              .size(11L)
              .build())
          .block();
    }

    @Test
    void shouldSaveProduct() {
      Product givenProduct = Product.builder()
          .name("Test product save name")
          .description("Test desc")
          .price(new BigDecimal("696.323"))
          .imageId(image.getId())
          .build();

      Product saveProduct = productRepository.save(givenProduct)
          .doOnNext(dbProduct -> assertAll(
              () -> assertNotNull(dbProduct),
              () -> assertNotNull(dbProduct.getId()),
              () -> assertThat(dbProduct)
                  .usingRecursiveComparison()
                  .ignoringFields("id")
                  .isEqualTo(givenProduct))
          ).block();

      assertNotNull(saveProduct);
      productRepository.findById(saveProduct.getId())
          .doOnNext(actualProduct -> assertThat(actualProduct)
              .usingRecursiveComparison()
              .isEqualTo(saveProduct))
          .subscribe();
    }

  }

}