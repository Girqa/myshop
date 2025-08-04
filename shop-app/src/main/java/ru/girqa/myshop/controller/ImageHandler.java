package ru.girqa.myshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.service.store.ImageService;

@Component
@RequiredArgsConstructor
public class ImageHandler {

  private final ImageService imageService;

  public Mono<ServerResponse> getImage(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));
    return imageService.findById(id)
        .map(Image::getData)
        .flatMap(data -> ServerResponse.ok()
            .bodyValue(data)
        );
  }
}
