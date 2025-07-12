package ru.girqa.myshop.model.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateProductDto(
    @NotBlank String name,
    @NotBlank String description,
    @Positive BigDecimal price
) {

}
