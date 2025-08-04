package ru.girqa.myshop.model.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPreviewDto(Long id, LocalDateTime createdAt, BigDecimal price) {

}
