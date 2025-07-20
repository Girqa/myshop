package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
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
import ru.girqa.myshop.repository.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

  @Mock
  ImageRepository imageRepositoryMock;
  @InjectMocks
  ImageService imageService;

  @BeforeEach
  void setUpMocks() {
    Mockito.reset(imageRepositoryMock);
  }

  @Test
  void shouldFindImageById() {
    final Long ID = 32L;
    final Image expected = Image.builder()
        .id(3L)
        .name("Net")
        .data("WWW".getBytes(StandardCharsets.UTF_8))
        .size(3L)
        .build();
    when(imageRepositoryMock.findById(ID))
        .thenReturn(Mono.just(expected));

    StepVerifier.create(imageService.findById(ID))
        .assertNext(found -> assertThat(found)
            .isSameAs(expected))
        .verifyComplete();
  }

  @Test
  void shouldNotFindImageById() {
    when(imageRepositoryMock.findById(anyLong()))
        .thenReturn(Mono.empty());

    StepVerifier.create(imageService.findById(552L))
        .verifyError(ShopEntityNotFoundException.class);
  }

}