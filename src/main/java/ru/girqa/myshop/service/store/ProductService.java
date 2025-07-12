package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;
import ru.girqa.myshop.repository.ImageRepository;
import ru.girqa.myshop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  private final ImageRepository imageRepository;

  @Transactional(readOnly = true)
  public Mono<Page<Product>> findAllForPage(@NonNull ProductPageRequest request) {
    if (request.getSearchName() == null || request.getSearchName().isBlank()) {
      return productRepository.findAll(request.toPageRequest());
    } else {
      return productRepository.findAllByName(request.getSearchName(), request.toPageRequest());
    }
  }

  @Transactional(readOnly = true)
  public Mono<Product> findById(@NonNull Long id) {
    return productRepository.findById(id)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new));
  }

  @Transactional
  public Mono<Product> save(@NonNull Product product) {
    return imageRepository.save(product.getImage())
        .map(image -> {
          product.setImageId(image.getId());
          return product;
        })
        .flatMap(productRepository::save);
  }

}
