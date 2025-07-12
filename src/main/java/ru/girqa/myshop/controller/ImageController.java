package ru.girqa.myshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.service.store.ImageService;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

  private final ImageService imageService;

  @GetMapping(path = "/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
  public Mono<byte[]> getImage(@PathVariable Long id) {
    return imageService.findById(id)
        .map(Image::getData);
  }

}
