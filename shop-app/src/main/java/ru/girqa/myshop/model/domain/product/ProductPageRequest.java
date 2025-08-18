package ru.girqa.myshop.model.domain.product;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Sort;
import ru.girqa.myshop.model.domain.sort.ProductSort;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageRequest {

  private int page = 0;

  private int pageSize = 10;

  @Builder.Default
  private String searchName = null;

  @Builder.Default
  private List<ProductSort> sorts = new ArrayList<>();

  public Sort getSort() {
    if (sorts.isEmpty()) {
      return Sort.unsorted();
    } else {
      List<Sort.Order> sortOrders = sorts.stream()
          .map(s -> switch (s.direction().getDirection()) {
            case ASC -> Sort.Order.asc(s.param().getFieldName());
            case DESC -> Sort.Order.desc(s.param().getFieldName());
          })
          .toList();
      return Sort.by(sortOrders);
    }
  }

  public long getOffset() {
    return (long) page * (long) pageSize;
  }
}
