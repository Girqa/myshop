package ru.girqa.myshop.controller;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.bucket.BucketProductAmount;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.domain.product.ProductPageRequest;
import ru.girqa.myshop.model.domain.product.ProductsPage;
import ru.girqa.myshop.model.domain.sort.ProductSort;
import ru.girqa.myshop.model.domain.sort.ProductSortParam;
import ru.girqa.myshop.model.domain.sort.SortDirection;
import ru.girqa.myshop.model.dto.product.ProductPageRequestDto;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

class MainPageHandlerTest extends BaseIntegrationTest {

  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  ProductService productServiceMock;

  @MockitoBean
  BucketService bucketServiceMock;

  final Long USER_ID = 3L;

  List<Product> products;

  ProductsPage page;

  Bucket bucket;

  ProductPageRequestDto givenRequest;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(productServiceMock, bucketServiceMock);
  }

  @BeforeEach
  void setUpData() {
    givenRequest = ProductPageRequestDto.builder()
        .page(2)
        .pageSize(20)
        .searchName("Some searcher")
        .nameSort(SortDirection.DESC)
        .priceSort(SortDirection.ASC)
        .build();

    ProductPageRequest domainRequest = ProductPageRequest.builder()
        .page(2)
        .pageSize(20)
        .searchName("Some searcher")
        .sorts(List.of(
            new ProductSort(ProductSortParam.NAME, SortDirection.DESC),
            new ProductSort(ProductSortParam.PRICE, SortDirection.ASC)
        )).build();

    products = List.of(
        Product.builder()
            .id(5L)
            .name("P1")
            .description("Nice P1 desc")
            .price(new BigDecimal("6124.23"))
            .image(Image.builder()
                .id(91L)
                .name("Im1")
                .size(55L)
                .data("Glad to see u".getBytes(StandardCharsets.UTF_8))
                .build())
            .build(),
        Product.builder()
            .id(73L)
            .name("P2")
            .description("Bad P2 desc")
            .price(new BigDecimal("3772.51"))
            .image(Image.builder()
                .id(141L)
                .name("2mI")
                .size(80L)
                .data("I too".getBytes(StandardCharsets.UTF_8))
                .build())
            .build()
    );

    page = new ProductsPage(products, domainRequest, 5);

    bucket = Bucket.builder()
        .id(1L)
        .userId(5L)
        .products(List.of(
            BucketProductAmount.builder()
                .amount(3)
                .product(products.getLast())
                .build()))
        .build();
  }

  @Test
  @SneakyThrows
  @WithUserDetails(value = USER_USERNAME, userDetailsServiceBeanName = "inMemoryUserDetailsService")
  void shouldReturnMainPage() {
    when(productServiceMock.findAllForPage(any()))
        .thenReturn(Mono.just(page));
    when(bucketServiceMock.findFilledOrCreateByUserId(anyLong()))
        .thenReturn(Mono.just(bucket));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/")
            .queryParam("user_id", USER_ID.toString())
            .queryParam("page", String.valueOf(givenRequest.getPage()))
            .queryParam("pageSize", String.valueOf(givenRequest.getPageSize()))
            .queryParam("searchName", givenRequest.getSearchName())
            .queryParam("nameSort", givenRequest.getNameSort().name())
            .queryParam("priceSort", givenRequest.getPriceSort().name())
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.TEXT_HTML)
        .expectBody()
        .xpath("//input[@name='searchName'][@value='%s']".formatted(givenRequest.getSearchName()))
        .exists()
        .xpath("//input[@name='nameSort'][@value='']").exists()
        .xpath("//input[@name='priceSort'][@value='DESC']").exists()
        .xpath("//option[@selected='selected']").string(
            containsString(String.valueOf(givenRequest.getPageSize())))
        .xpath("//article/div[contains(@class, 'card')]").nodeCount(products.size());
  }

}