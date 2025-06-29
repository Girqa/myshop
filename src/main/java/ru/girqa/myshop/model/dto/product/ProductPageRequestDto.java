package ru.girqa.myshop.model.dto.product;

import lombok.Data;
import ru.girqa.myshop.model.domain.sort.SortDirection;

@Data
public class ProductPageRequestDto {

  public static int DEFAULT_PAGE = 1;
  public static int DEFAULT_PAGE_SIZE = 10;

  private int page;
  private int pageSize;
  private String searchName;
  private SortDirection nameSort;
  private SortDirection priceSort;

  public ProductPageRequestDto() {
    page = DEFAULT_PAGE;
    pageSize = DEFAULT_PAGE_SIZE;
  }
}
