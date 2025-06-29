package ru.girqa.myshop.model.dto.product;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Long imageId,
    Integer countInBucket
) {

}
