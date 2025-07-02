package ru.girqa.myshop.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.service.store.ImageService;

@WebMvcTest(controllers = ImageController.class)
class ImageControllerTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  ImageService imageServiceMock;

  @Test
  @SneakyThrows
  void shouldFindImage() {
    final Long IMAGE_ID = 765L;
    final byte[] DATA = "Best data".getBytes(StandardCharsets.UTF_8);
    when(imageServiceMock.findById(IMAGE_ID))
        .thenReturn(Image.builder()
            .data(DATA)
            .build());

    MvcResult result = mvc.perform(get("/api/v1/image/{id}", IMAGE_ID)
            .accept(MediaType.IMAGE_JPEG))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andReturn();

    byte[] responseContent = result.getResponse().getContentAsByteArray();
    assertArrayEquals(DATA, responseContent);
  }

}