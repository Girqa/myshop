package ru.girqa.myshop.service.store;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.exception.ShopEntityNotFoundException;
import ru.girqa.myshop.model.domain.Image;
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
  public Product create(@NonNull Product product) {
    Image savedImage = imageRepository.save(product.getImage());
    return productRepository.save(product.toBuilder()
        .image(savedImage)
        .build());
  }

  @Transactional
  public void delete(@NonNull Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(ShopEntityNotFoundException::new);
    Image image = product.getImage();

    productRepository.delete(product);

    if (image.getReferencedOrderProducts().isEmpty()) {
      imageRepository.delete(image);
    }
  }

}
