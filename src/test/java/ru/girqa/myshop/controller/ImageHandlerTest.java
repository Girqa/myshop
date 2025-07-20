package ru.girqa.myshop.controller;

import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.common.BaseIntegrationTest;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.service.store.ImageService;

class ImageHandlerTest extends BaseIntegrationTest {

  @MockitoBean
  ImageService imageServiceMock;

  @Autowired
  WebTestClient webTestClient;

  @Test
  void shouldGetImageData() {
    when(imageServiceMock.findById(5L))
        .thenReturn(Mono.just(Image.builder()
            .name("Img")
            .size(9L)
            .data("Some data".getBytes(StandardCharsets.UTF_8))
            .build()));

    webTestClient.get()
        .uri("/api/v1/image/5")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class).isEqualTo("Some data");
  }

}