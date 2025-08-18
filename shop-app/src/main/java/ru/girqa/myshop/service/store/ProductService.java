package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.domain.product.ProductPageRequest;
import ru.girqa.myshop.model.domain.product.ProductsPage;
import ru.girqa.myshop.repository.ImageRepository;
import ru.girqa.myshop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  private final ImageRepository imageRepository;

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "products_page", key = "#request.toString()")
  public Mono<ProductsPage> findAllForPage(@NonNull ProductPageRequest request) {
    if (request.getSearchName() == null || request.getSearchName().isBlank()) {
      return productRepository.findAll(request);
    } else {
      return productRepository.findAllByName(request.getSearchName(), request);
    }
  }

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "product_item", key = "#id")
  public Mono<Product> findById(@NonNull Long id) {
    return productRepository.findById(id)
        .switchIfEmpty(Mono.error(ShopEntityNotFoundException::new));
  }

  @Transactional
  @CacheEvict(cacheNames = "products_page", allEntries = true)
  public Mono<Product> save(@NonNull Product product) {
    return imageRepository.save(product.getImage())
        .map(image -> {
          product.setImageId(image.getId());
          return product;
        })
        .flatMap(productRepository::save);
  }

}
