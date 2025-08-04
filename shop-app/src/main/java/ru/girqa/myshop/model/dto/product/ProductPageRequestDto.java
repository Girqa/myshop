package ru.girqa.myshop.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.girqa.myshop.model.domain.sort.SortDirection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageRequestDto {

  public static int DEFAULT_PAGE = 1;
  public static int DEFAULT_PAGE_SIZE = 10;

  @Builder.Default
  private Integer page = DEFAULT_PAGE;
  @Builder.Default
  private Integer pageSize = DEFAULT_PAGE_SIZE;
  private String searchName;
  private SortDirection nameSort;
  private SortDirection priceSort;

}
