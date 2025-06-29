package ru.girqa.myshop.model.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
    Long id,
    LocalDateTime createdAt,
    BigDecimal price,
    List<OrderProductDto> products
) {

}
