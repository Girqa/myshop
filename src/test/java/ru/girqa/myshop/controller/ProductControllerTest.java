package ru.girqa.myshop.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.mapper.ImageMapper;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@WebMvcTest(
    controllers = ProductController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {ProductMapper.class, ImageMapper.class}
    )
)
class ProductControllerTest {

  @MockitoBean
  ProductService productServiceMock;

  @MockitoBean
  BucketService bucketServiceMock;

  @Autowired
  MockMvc mvc;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(productServiceMock, bucketServiceMock);
  }

  @Test
  @SneakyThrows
  void shouldCreateProduct() {
    final Product newProduct = getProduct(null, null);

    final Product dbProduct = newProduct.toBuilder().build();
    dbProduct.setId(67L);

    when(productServiceMock.save(any()))
        .thenReturn(dbProduct);

    mvc.perform(multipart(HttpMethod.POST, "/product")
            .file(new MockMultipartFile(
                "image",
                newProduct.getImage().getName(),
                null,
                newProduct.getImage().getData()))
            .queryParam("name", newProduct.getName())
            .queryParam("description", newProduct.getDescription())
            .queryParam("price", newProduct.getPrice().toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlTemplate("/product/{id}", dbProduct.getId()));
  }

  @Test
  @SneakyThrows
  void shouldGetProduct() {
    final Long PRODUCT_ID = 82L;
    final Long IMAGE_ID = 119L;
    final Product product = getProduct(PRODUCT_ID, IMAGE_ID);
    when(productServiceMock.findById(PRODUCT_ID))
        .thenReturn(product);
    when(bucketServiceMock.findOrCreateByUserId(anyLong()))
        .thenReturn(Bucket.builder().build());

    mvc.perform(get("/product/{id}", PRODUCT_ID))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("product/product"))
        .andExpect(xpath("//h3").string(containsString(product.getName())))
        .andExpect(xpath("//img[contains(@src, '/api/v1/image/%d')]".formatted(IMAGE_ID)).exists())
        .andExpect(xpath("//p")
            .nodeCount(product.getDescription().split("\n").length + 1))
        .andExpect(xpath("//p[contains(@class, 'card-subtitle')]")
            .string(containsString(product.getPrice().toString())));
  }

  private Product getProduct(Long id, Long imageId) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Product product = Product.builder()
        .name("Pname" + random.nextDouble())
        .description("Pdesc\nription")
        .price(BigDecimal.valueOf(random.nextDouble()))
        .image(Image.builder()
            .name("ImName" + random.nextInt())
            .data("Good day tooday".getBytes(StandardCharsets.UTF_8))
            .size((long) "Good day tooday".length())
            .build()
        ).build();
    product.setId(id);
    product.getImage().setId(imageId);
    return product;
  }

}