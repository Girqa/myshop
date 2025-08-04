package ru.girqa.myshop.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.model.domain.OrderProduct;
import ru.girqa.myshop.model.dto.order.OrderDto;
import ru.girqa.myshop.model.dto.order.OrderPreviewDto;
import ru.girqa.myshop.model.dto.order.OrderProductDto;

@Mapper(componentModel = ComponentModel.SPRING)
public interface OrderMapper {

  OrderDto toDto(Order order);

  OrderProductDto toDto(OrderProduct orderProduct);

  OrderPreviewDto toPreview(Order order);

}
