package ru.girqa.myshop.model.dto.product;

import java.math.BigDecimal;

public record ProductPreviewDto(
    Long id,
    String name,
    BigDecimal price,
    Long imageId,
    Integer countInBucket
) {

}
