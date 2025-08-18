package ru.girqa.myshop.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import ru.girqa.myshop.model.domain.bucket.BucketProductAmount;
import ru.girqa.myshop.model.domain.order.OrderProduct;

@Mapper(componentModel = ComponentModel.SPRING)
public interface OrderProductMapper {


  @Mapping(target = "orderId", ignore = true)
  @Mapping(target = "price", source = "product.price")
  @Mapping(target = "name", source = "product.name")
  @Mapping(target = "imageId", source = "product.imageId")
  @Mapping(target = "description", source = "product.description")
  OrderProduct toOrder(BucketProductAmount bucketProductAmount);

  List<OrderProduct> toOrder(List<BucketProductAmount> bucketProductAmounts);
}
