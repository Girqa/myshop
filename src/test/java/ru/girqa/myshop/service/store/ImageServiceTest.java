package ru.girqa.myshop.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
  void shouldFindImage() {
    final Long IMAGE_ID = 773L;
    Image image = Image.builder()
        .name("Image name")
        .data("Hello world".getBytes(StandardCharsets.UTF_8))
        .size(11L)
        .build();

    when(imageRepositoryMock.findById(IMAGE_ID))
        .thenReturn(Optional.of(image));

    Image foundImage = assertDoesNotThrow(() -> imageService.findById(IMAGE_ID));
    assertThat(foundImage)
        .usingRecursiveComparison()
        .isEqualTo(image);
  }

  @Test
  void shouldThrowNotFoundException() {
    when(imageRepositoryMock.findById(any()))
        .thenReturn(Optional.empty());

    assertThrows(
        ShopEntityNotFoundException.class,
        () -> imageService.findById(552L)
    );
  }

}