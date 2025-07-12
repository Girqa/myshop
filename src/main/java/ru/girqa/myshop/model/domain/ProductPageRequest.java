package ru.girqa.myshop.model.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.girqa.myshop.model.domain.sort.ProductSort;

@Getter
@Builder
@AllArgsConstructor
public class ProductPageRequest {

  private final int page;

  private final int pageSize;

  @Builder.Default
  private String searchName = null;

  @Builder.Default
  private List<ProductSort> sorts = new ArrayList<>();

  public Pageable toPageRequest() {
    if (sorts.isEmpty()) {
      return PageRequest.of(page, pageSize);
    } else {
      List<Sort.Order> sortOrders = sorts.stream()
          .map(s -> switch (s.direction().getDirection()) {
            case ASC -> Sort.Order.asc(s.param().getFieldName());
            case DESC -> Sort.Order.desc(s.param().getFieldName());
          })
          .toList();
      return PageRequest.of(page, pageSize, Sort.by(sortOrders));
    }
  }
}
