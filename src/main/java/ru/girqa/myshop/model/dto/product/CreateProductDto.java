package ru.girqa.myshop.model.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

public record CreateProductDto(
    @NotBlank String name,
    @NotBlank String description,
    @Positive BigDecimal price,
    @NotNull MultipartFile image
) {

}
