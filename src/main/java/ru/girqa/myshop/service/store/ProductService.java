package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;
import ru.girqa.myshop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  @Transactional(readOnly = true)
  public Page<Product> findAllForPage(@NonNull ProductPageRequest request) {
    return productRepository.findAll(
        request.toSpecification(),
        request.toPageRequest()
    );
  }

  @Transactional(readOnly = true)
  public Product findById(@NonNull Long id) {
    return productRepository.findById(id)
        .orElseThrow(ShopEntityNotFoundException::new);
  }

  @Transactional
  public Product save(@NonNull Product product) {
    return productRepository.save(product);
  }

}
