package ru.girqa.myshop.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.dto.product.CreateProductDto;
import ru.girqa.myshop.model.dto.product.ProductDto;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ProductMapper {

  @Mapping(target = "image", ignore = true)
  @Mapping(target = "imageId", ignore = true)
  Product toDomain(CreateProductDto dto);

  @Mapping(target = "id", source = "product.id")
  @Mapping(target = "imageId", source = "product.imageId")
  @Mapping(target = "countInBucket", expression = "java(getProductInBucketAmount(product, bucket))")
  ProductPreviewDto toPreview(Product product, Bucket bucket);

  default List<ProductPreviewDto> toPreview(List<Product> products, Bucket bucket) {
    return products.stream()
        .map(p -> toPreview(p, bucket))
        .toList();
  }

  @Mapping(target = "id", source = "product.id")
  @Mapping(target = "imageId", source = "product.imageId")
  @Mapping(target = "countInBucket", expression = "java(getProductInBucketAmount(product, bucket))")
  ProductDto toDto(Product product, Bucket bucket);

  default Integer getProductInBucketAmount(Product product, Bucket bucket) {
    return bucket.amountOfProduct(product.getId())
        .orElse(null);
  }
}
