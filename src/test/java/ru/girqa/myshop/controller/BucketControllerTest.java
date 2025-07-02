package ru.girqa.myshop.controller;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.math.BigDecimal;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductAmount;
import ru.girqa.myshop.model.mapper.ImageMapper;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;

@WebMvcTest(controllers = BucketController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = { ProductMapper.class, ImageMapper.class }
    )
)
class BucketControllerTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  BucketService bucketServiceMock;

  @Test
  @SneakyThrows
  void shouldPresentMainPage() {
    Image image = Image.builder().build();
    image.setId(2L);

    Product product = Product.builder()
        .name("Pname")
        .price(new BigDecimal("62.23"))
        .description("Desc")
        .image(image)
        .build();
    product.setId(5L);

    Bucket bucket = Bucket.builder()
        .products(List.of(
            ProductAmount.builder()
                .amount(2)
                .product(product)
                .build()
        )).build();

    when(bucketServiceMock.findOrCreateByUserId(any()))
        .thenReturn(bucket);

    mvc.perform(get("/bucket"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("bucket/bucket"))
        .andExpect(xpath("//h4").string(containsString(bucket.getTotalPrice().toString())))
        .andExpect(
            xpath("//img[contains(@src, '/api/v1/image/%d')]".formatted(image.getId())).exists())
        .andExpect(xpath("//a[contains(@href, '/product/%d')]".formatted(product.getId())).exists())
        .andExpect(xpath("//h5").string(containsString(product.getName())))
        .andExpect(xpath("//p[contains(@class, 'card-subtitle')]").string(
            containsString(product.getPrice().toString())));
  }

  @Test
  @SneakyThrows
  void shouldAddProduct() {
    final Long BUCKET_ID = 123L, PRODUCT_ID = 62L;
    mvc.perform(post("/bucket/{bucket_id}/{product_id}", BUCKET_ID, PRODUCT_ID))
        .andExpect(status().isAccepted());

    verify(bucketServiceMock, times(1))
        .addProduct(BUCKET_ID, PRODUCT_ID);
  }

  @Test
  @SneakyThrows
  void shouldRemoveProduct() {
    final Long BUCKET_ID = 112L, PRODUCT_ID = 652L;
    mvc.perform(delete("/bucket/{bucket_id}/{product_id}", BUCKET_ID, PRODUCT_ID))
        .andExpect(status().isAccepted());

    verify(bucketServiceMock, times(1))
        .removeProduct(BUCKET_ID, PRODUCT_ID);
  }

  @Test
  @SneakyThrows
  void shouldIncrementProductAmount() {
    final Long BUCKET_ID = 9L, PRODUCT_ID = 31L;
    mvc.perform(put("/bucket/{bucket_id}/{product_id}", BUCKET_ID, PRODUCT_ID)
            .queryParam("increase", "true"))
        .andExpect(status().isAccepted());

    verify(bucketServiceMock, times(1))
        .incrementProductCount(BUCKET_ID, PRODUCT_ID);
  }

  @Test
  @SneakyThrows
  void shouldDecrementProductAmount() {
    final Long BUCKET_ID = 33L, PRODUCT_ID = 65L;
    mvc.perform(put("/bucket/{bucket_id}/{product_id}", BUCKET_ID, PRODUCT_ID)
            .queryParam("increase", "false"))
        .andExpect(status().isAccepted());

    verify(bucketServiceMock, times(1))
        .decrementProductCount(BUCKET_ID, PRODUCT_ID);
  }

}