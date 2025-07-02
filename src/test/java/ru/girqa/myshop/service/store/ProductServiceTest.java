package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;
import ru.girqa.myshop.model.domain.Product_;
import ru.girqa.myshop.model.domain.search.LikeValueSearch;
import ru.girqa.myshop.model.domain.sort.ProductSort;
import ru.girqa.myshop.model.domain.sort.ProductSortParam;
import ru.girqa.myshop.model.domain.sort.SortDirection;
import ru.girqa.myshop.repository.ProductRepository;

class ProductServiceTest extends BaseIntegrationTest {

  @Autowired
  ProductRepository repository;

  @Autowired
  ProductService productService;

  List<Product> products;

  @BeforeEach
  void setUpProducts() {
    products = List.of(
        Product.builder()
            .name("P1")
            .description("d1")
            .price(new BigDecimal("3.21"))
            .image(Image.builder()
                .name("im1")
                .data("Hello world!".getBytes(StandardCharsets.UTF_8))
                .size(12L)
                .build())
            .build(),
        Product.builder()
            .name("P2")
            .description("d2")
            .price(new BigDecimal("551.22"))
            .image(Image.builder()
                .name("im2")
                .data("Hey bro".getBytes(StandardCharsets.UTF_8))
                .size(7L)
                .build())
            .build()
    );

    products = repository.saveAll(products);
  }

  @AfterEach
  void tearDownProducts() {
    repository.deleteAll();
  }

  @Nested
  class SearchTests {

    @Test
    @Transactional
    void shouldFindById() {
      final Long ID = products.getFirst().getId();
      Product product = productService.findById(ID);
      assertThat(product)
          .usingRecursiveComparison()
          .isEqualTo(products.getFirst());
    }

    @Test
    void shouldThrowNotFoundException() {
      assertThrows(
          ShopEntityNotFoundException.class,
          () -> productService.findById(123456L)
      );
    }
  }

  @Nested
  @Transactional
  class PageTests {

    @Test
    void shouldRequestPageWithAllProducts() {
      ProductPageRequest request = ProductPageRequest.builder()
          .page(0)
          .pageSize(10)
          .build();

      Page<Product> page = productService.findAllForPage(request);

      assertEquals(2, page.getNumberOfElements());
      assertEquals(0, page.getNumber());
      assertEquals(1, page.getTotalPages());

      assertEquals(products, page.getContent());
    }

    @Test
    void shouldRequestPageWithOneProduct() {
      ProductPageRequest request = ProductPageRequest.builder()
          .page(0)
          .pageSize(5)
          .filters(List.of(
              new LikeValueSearch<>(Product_.NAME, products.getFirst().getName())
          ))
          .build();

      Page<Product> page = productService.findAllForPage(request);

      assertEquals(1, page.getNumberOfElements());
      assertEquals(0, page.getNumber());
      assertEquals(1, page.getTotalPages());

      assertEquals(products.getFirst(), page.getContent().getFirst());
    }

    @Test
    void shouldRequestPageWithSort() {
      ProductPageRequest request = ProductPageRequest.builder()
          .page(0)
          .pageSize(12)
          .sorts(List.of(
              new ProductSort(ProductSortParam.PRICE, SortDirection.DESC)
          ))
          .build();

      Page<Product> page = productService.findAllForPage(request);

      assertEquals(2, page.getNumberOfElements());
      assertEquals(0, page.getNumber());
      assertEquals(1, page.getTotalPages());

      assertEquals(
          products.stream()
              .sorted(Comparator.comparing(Product::getPrice).reversed())
              .toList(),
          page.getContent()
      );
    }
  }

}