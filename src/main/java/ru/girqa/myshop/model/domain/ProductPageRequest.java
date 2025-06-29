package ru.girqa.myshop.model.domain;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.girqa.myshop.model.domain.search.SearchCriteria;
import ru.girqa.myshop.model.domain.sort.ProductSort;

@Getter
@Builder
@AllArgsConstructor
public class ProductPageRequest {

  private final int page;

  private final int pageSize;

  @Builder.Default
  private List<SearchCriteria<Product>> filters = new ArrayList<>();

  @Builder.Default
  private List<ProductSort> sorts = new ArrayList<>();

  public Specification<Product> toSpecification() {
    return (root, query, cb) -> {
      if (filters.isEmpty()) {
        return cb.conjunction();
      }

      List<Predicate> predicates = filters.stream()
          .map(SearchCriteria::toSpecification)
          .map(specification -> specification.toPredicate(root, query, cb))
          .toList();

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

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
