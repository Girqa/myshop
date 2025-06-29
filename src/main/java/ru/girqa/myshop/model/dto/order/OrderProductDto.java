package ru.girqa.myshop.model.dto.order;

import java.math.BigDecimal;

public record OrderProductDto(
     Long id,
     String name,
     String description,
     BigDecimal price,
     Integer amount,
     Long imageId
) {

}
