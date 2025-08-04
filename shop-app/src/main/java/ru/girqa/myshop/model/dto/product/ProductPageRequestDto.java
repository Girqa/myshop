package ru.girqa.myshop.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.girqa.myshop.model.domain.sort.SortDirection;

@Data
@Builder
@AllArgsConstructor
public class ProductPageRequestDto {

  public static int DEFAULT_PAGE = 1;
  public static int DEFAULT_PAGE_SIZE = 10;

  private int page;
  private int pageSize;
  private String searchName;
  private SortDirection nameSort;
  private SortDirection priceSort;

}
