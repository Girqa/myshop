package ru.girqa.myshop.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

class ProductHandlerTest extends BaseIntegrationTest {

  @MockitoSpyBean
  BucketService bucketServiceSpy;
  @MockitoSpyBean
  ProductService productServiceSpy;

  @Autowired
  WebTestClient webTestClient;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(bucketServiceSpy, productServiceSpy);
  }


  @Test
  void shouldGetProduct() {
    Product givenProduct = getProduct();
    final Product dbProduct = productServiceSpy.save(givenProduct).block();

    assert dbProduct != null;
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/product/%d".formatted(dbProduct.getId()))
            .queryParam("user_id", "11")
            .build()
        )
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.TEXT_HTML)
        .expectBody(String.class).consumeWith(response -> {
          String body = response.getResponseBody();
          assertNotNull(body);
          assertTrue(body.contains(givenProduct.getName()));
          assertTrue(body.contains(givenProduct.getDescription()));
          assertTrue(body.contains(givenProduct.getPrice().toString()));
          assertTrue(body.contains(givenProduct.getImageId().toString()));
        });

    verify(productServiceSpy, times(1))
        .findById(dbProduct.getId());
    verify(bucketServiceSpy, times(1))
        .findFilledOrCreateByUserId(11L);
  }

  @Test
  void shouldCreateProduct() {
    Product givenProduct = getProduct();

    MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
    bodyBuilder.part("name", givenProduct.getName());
    bodyBuilder.part("description", givenProduct.getDescription());
    bodyBuilder.part("price", givenProduct.getPrice());
    bodyBuilder.part("image", givenProduct.getImage().getData()).filename(givenProduct.getName());

    webTestClient.post()
        .uri("/product")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .exchange()
        .expectStatus().isFound()
        .expectHeader().valueMatches("Location", "/product/[0-9]");

    verify(productServiceSpy, times(1))
        .save(any());
  }

  private Product getProduct() {
    return Product.builder()
        .name("PName")
        .description("PDesc")
        .price(new BigDecimal("42.21"))
        .image(Image.builder()
            .name("PImage")
            .size(4L)
            .data("DATA".getBytes(StandardCharsets.UTF_8))
            .build())
        .build();
  }

}