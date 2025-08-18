package ru.girqa.myshop.model.domain.product;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsPage {

  private List<Product> products;

  private ProductPageRequest pageRequest;

  private long totalPages;

}
