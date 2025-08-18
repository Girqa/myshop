package ru.girqa.myshop.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.bucket.BucketProductAmount;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.service.store.BucketService;


class BucketHandlerTest extends BaseIntegrationTest {

  @MockitoBean
  BucketService bucketServiceMock;

  @Autowired
  WebTestClient webTestClient;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(bucketServiceMock);
  }

  @Test
  @WithUserDetails(value = USER_USERNAME, userDetailsServiceBeanName = "inMemoryUserDetailsService")
  void shouldGetBucketPage() {
    Bucket bucket = getBucket();
    when(bucketServiceMock.findFilledOrCreateByUserId(bucket.getUserId()))
        .thenReturn(Mono.just(bucket));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/bucket")
            .queryParam("user_id", bucket.getUserId())
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.TEXT_HTML)
        .expectBody()
        .consumeWith(System.out::println)
        .xpath(String.format("//h4[text()='Полная цена = %sР']", bucket.getTotalPrice())).exists()
        .xpath("//div[contains(@class, 'row')]/div[contains(@class, 'col')]")
        .nodeCount(bucket.getProducts().size());
  }

  @Test
  @WithMockUser
  void shouldAddProduct() {

    final Long BUCKET_ID = 9L, PRODUCT_ID = 21L;
    when(bucketServiceMock.addProduct(BUCKET_ID, PRODUCT_ID))
        .thenReturn(Mono.empty());

    webTestClient.post()
        .uri("/bucket/{bucket_id}/{product_id}", BUCKET_ID, PRODUCT_ID)
        .exchange()
        .expectStatus().isAccepted();

    verify(bucketServiceMock, times(1))
        .addProduct(BUCKET_ID, PRODUCT_ID);
  }

  @Test
  @WithMockUser
  void shouldRemoveProduct() {
    final Long BUCKET_ID = 9L, PRODUCT_ID = 21L;
    when(bucketServiceMock.removeProduct(BUCKET_ID, PRODUCT_ID))
        .thenReturn(Mono.empty());

    webTestClient.delete()
        .uri("/bucket/{bucket_id}/{product_id}", BUCKET_ID, PRODUCT_ID)
        .exchange()
        .expectStatus().isAccepted();

    verify(bucketServiceMock, times(1))
        .removeProduct(BUCKET_ID, PRODUCT_ID);
  }

  @Test
  @WithMockUser
  void shouldIncrementProduct() {
    final Long BUCKET_ID = 9L, PRODUCT_ID = 21L;
    when(bucketServiceMock.incrementProductCount(BUCKET_ID, PRODUCT_ID))
        .thenReturn(Mono.empty());

    webTestClient.put()
        .uri(uriBuilder -> uriBuilder
            .pathSegment("bucket", BUCKET_ID.toString(), PRODUCT_ID.toString())
            .queryParam("increase", true)
            .build())
        .exchange()
        .expectStatus().isAccepted();

    verify(bucketServiceMock, times(1))
        .incrementProductCount(BUCKET_ID, PRODUCT_ID);
  }

  @Test
  @WithMockUser
  void shouldDecrementProduct() {
    final Long BUCKET_ID = 9L, PRODUCT_ID = 21L;
    when(bucketServiceMock.decrementProductCount(BUCKET_ID, PRODUCT_ID))
        .thenReturn(Mono.empty());

    webTestClient.put()
        .uri(uriBuilder -> uriBuilder
            .pathSegment("bucket", BUCKET_ID.toString(), PRODUCT_ID.toString())
            .queryParam("increase", false)
            .build())
        .exchange()
        .expectStatus().isAccepted();

    verify(bucketServiceMock, times(1))
        .decrementProductCount(BUCKET_ID, PRODUCT_ID);
  }

  private Bucket getBucket() {
    return Bucket.builder()
        .id(5L)
        .userId(USER_ID)
        .products(List.of(
            BucketProductAmount.builder()
                .amount(3)
                .productId(5L)
                .bucketId(14L)
                .product(Product.builder()
                    .id(5L)
                    .name("Pname")
                    .description("PDesc")
                    .price(new BigDecimal("51.21"))
                    .image(Image.builder()
                        .id(551L)
                        .build())
                    .build())
                .build(),
            BucketProductAmount.builder()
                .amount(5)
                .productId(7L)
                .bucketId(14L)
                .product(Product.builder()
                    .id(7L)
                    .name("Pname 2")
                    .description("PDesc 2")
                    .price(new BigDecimal("345.52"))
                    .image(Image.builder()
                        .id(325L)
                        .build())
                    .build())
                .build()
        ))
        .build();
  }
}