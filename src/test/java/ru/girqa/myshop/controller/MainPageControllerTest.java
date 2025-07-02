package ru.girqa.myshop.controller;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductAmount;
import ru.girqa.myshop.model.domain.sort.SortDirection;
import ru.girqa.myshop.model.dto.product.ProductPageRequestDto;
import ru.girqa.myshop.model.mapper.ImageMapper;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.model.mapper.ProductPageMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@WebMvcTest(controllers = MainPageController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {ProductMapper.class, ProductPageMapper.class, ImageMapper.class}
    ))
class MainPageControllerTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  ProductService productServiceMock;

  @MockitoBean
  BucketService bucketServiceMock;

  final Long USER_ID = 3L;

  List<Product> products;

  Page<Product> page;

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

    products = List.of(
        Product.builder()
            .name("P1")
            .description("Nice P1 desc")
            .price(new BigDecimal("6124.23"))
            .image(Image.builder()
                .name("Im1")
                .size(55L)
                .data("Glad to see u".getBytes(StandardCharsets.UTF_8))
                .build())
            .build(),
        Product.builder()
            .name("P2")
            .description("Bad P2 desc")
            .price(new BigDecimal("3772.51"))
            .image(Image.builder()
                .name("2mI")
                .size(80L)
                .data("I too".getBytes(StandardCharsets.UTF_8))
                .build())
            .build()
    );

    Product firstProduct = products.getFirst();
    firstProduct.setId(5L);
    firstProduct.getImage().setId(91L);

    Product secondProduct = products.getLast();
    secondProduct.setId(73L);
    secondProduct.getImage().setId(141L);

    page = new PageImpl<>(
        products, PageRequest.of(givenRequest.getPage(), givenRequest.getPageSize()), 5
    );

    bucket = Bucket.builder()
        .userId(5L)
        .products(List.of(
            ProductAmount.builder()
                .amount(3)
                .product(products.getLast())
                .build()))
        .build();
  }

  @Test
  @SneakyThrows
  void shouldReturnMainPage() {
    when(productServiceMock.findAllForPage(any()))
        .thenReturn(page);
    when(bucketServiceMock.findOrCreateByUserId(anyLong()))
        .thenReturn(bucket);

    mvc.perform(get("/")
            .queryParam("user_id", USER_ID.toString())
            .queryParam("page", String.valueOf(givenRequest.getPage()))
            .queryParam("pageSize", String.valueOf(givenRequest.getPageSize()))
            .queryParam("searchName", givenRequest.getSearchName())
            .queryParam("nameSort", givenRequest.getNameSort().name())
            .queryParam("priceSort", givenRequest.getPriceSort().name()))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("product/products"))
        .andExpect(xpath("//input[@name='searchName'][@value='%s']".formatted(givenRequest.getSearchName())).exists())
        .andExpect(xpath("//input[@name='nameSort'][@value='']").exists())
        .andExpect(xpath("//input[@name='priceSort'][@value='DESC']").exists())
        .andExpect(xpath("//option[@selected='selected']").string(
            containsString(String.valueOf(givenRequest.getPageSize()))))
        .andExpect(xpath("//article/div[contains(@class, 'card')]").nodeCount(products.size()))
        .andExpect(product(1, products.getFirst(), bucket))
        .andExpect(product(2, products.getLast(), bucket))
    ;
  }

  private ResultMatcher product(int orderId, Product product, Bucket bucket) {
    return result -> {
      String basePath = "//div[contains(@class, 'col')][%d]".formatted(orderId);
      xpath(basePath + "//img[@src='/api/v1/image/%s']".formatted(product.getImage().getId()))
          .exists().match(result);
      xpath(basePath + "//a[@href='/product/%s']".formatted(product.getId()))
          .exists().match(result);
      xpath(basePath + "//h5").string(containsString(product.getName())).match(result);
      xpath(basePath + "//p").string(containsString(product.getPrice().toString())).match(result);

      Optional<Integer> amountOp = bucket.amountOfProduct(product.getId());
      if (amountOp.isEmpty()) {
        xpath(basePath + "//div[@class='card-footer']/div[contains(@style, 'none')][0]");
        xpath(basePath + "//div[@class='card-footer']/div[contains(@style, 'block')][1]");
      } else {
        xpath(basePath + "//div[@class='card-footer']/div[contains(@style, 'block')][0]");
        xpath(basePath + "//div[@class='card-footer']/div[contains(@style, 'none')][1]");
      }
    };
  }
}