package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.repository.ImageRepository;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final ImageRepository imageRepository;

  public Mono<Image> findById(@NonNull Long id) {
    return imageRepository.findById(id)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new));
  }

}
