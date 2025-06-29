package ru.girqa.myshop.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.domain.ProductAmount;

@Mapper(componentModel = ComponentModel.SPRING)
public interface OrderProductMapper {


  @Mapping(target = "order", ignore = true)
  @Mapping(target = "price", source = "product.price")
  @Mapping(target = "name", source = "product.name")
  @Mapping(target = "image", source = "product.image")
  @Mapping(target = "description", source = "product.description")
  OrderProduct toOrder(ProductAmount productAmount);

  List<OrderProduct> toOrder(List<ProductAmount> productAmounts);
}
