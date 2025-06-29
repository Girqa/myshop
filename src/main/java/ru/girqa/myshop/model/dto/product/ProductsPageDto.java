package ru.girqa.myshop.model.dto.product;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductsPageDto {

  private ProductPageRequestDto request;
  private int totalPages;
  private List<ProductPreviewDto> products;
}
