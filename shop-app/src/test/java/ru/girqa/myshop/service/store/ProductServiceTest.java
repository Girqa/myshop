package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;
import ru.girqa.myshop.model.domain.ProductsPage;
import ru.girqa.myshop.repository.ImageRepository;
import ru.girqa.myshop.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  ProductRepository productRepositoryMock;
  @Mock
  ImageRepository imageRepositoryMock;
  @InjectMocks
  ProductService productService;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(productRepositoryMock, imageRepositoryMock);
  }

  @Nested
  class SearchTests {

    @Test
    void shouldFindPageByPageable() {
      final ProductsPage expectedPage = ProductsPage.builder()
          .products(List.of())
          .build();

      when(productRepositoryMock.findAll(any()))
          .thenReturn(Mono.just(expectedPage));

      StepVerifier.create(productService.findAllForPage(ProductPageRequest.builder()
          .page(1)
          .pageSize(5)
          .build())
      ).assertNext(page -> assertThat(page)
          .isSameAs(expectedPage)
      ).verifyComplete();

      verify(productRepositoryMock, times(1))
          .findAll(any());
      verify(productRepositoryMock, never())
          .findAllByName(any(), any());
    }

    @Test
    void shouldFindPageByPageableAndName() {
      final ProductsPage expectedPage = ProductsPage.builder()
          .products(List.of())
          .build();

      when(productRepositoryMock.findAllByName(any(), any()))
          .thenReturn(Mono.just(expectedPage));

      StepVerifier.create(productService.findAllForPage(ProductPageRequest.builder()
              .page(2)
              .pageSize(10)
              .searchName("The Bull")
              .build())
          ).assertNext(page -> assertThat(page)
              .isSameAs(expectedPage))
          .verifyComplete();

      verify(productRepositoryMock, times(1))
          .findAllByName(eq("The Bull"), any());
      verify(productRepositoryMock, never())
          .findAll(any());
    }

    @Test
    void shouldFindById() {
      final Long productId = 23L;
      final Product expectedProduct = Product.builder()
          .id(productId)
          .build();

      when(productRepositoryMock.findById(productId))
          .thenReturn(Mono.just(expectedProduct));

      StepVerifier.create(productService.findById(productId))
          .assertNext(product -> assertThat(product).isSameAs(expectedProduct))
          .verifyComplete();
    }

    @Test
    void shouldNotFindById() {
      when(productRepositoryMock.findById(anyLong()))
          .thenReturn(Mono.empty());

      StepVerifier.create(productService.findById(991L))
          .verifyError(ShopEntityNotFoundException.class);
    }

  }

  @Nested
  class SaveTests {

    @Test
    void shouldSaveProduct() {
      final Product product = Product.builder()
          .image(Image.builder()
              .build())
          .build();

      when(imageRepositoryMock.save(any()))
          .then(call -> {
            Image image = call.getArgument(0);
            image.setId(51L);
            return Mono.just(image);
          });
      when(productRepositoryMock.save(any()))
          .then(call -> Mono.just(call.getArgument(0)));

      StepVerifier.create(productService.save(product))
          .assertNext(saved -> assertThat(saved)
              .isSameAs(product)
              .extracting(Product::getImageId)
              .isEqualTo(51L))
          .verifyComplete();
    }

  }
}