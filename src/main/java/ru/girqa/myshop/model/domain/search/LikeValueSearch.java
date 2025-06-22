package ru.girqa.myshop.model.domain.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.girqa.myshop.model.domain.BaseEntity;

@RequiredArgsConstructor
public class LikeValueSearch<T extends BaseEntity> implements SearchCriteria<T> {

  private final String field;

  private final String value;

  @Override
  public Specification<T> toSpecification() {
    return ((root, query, cb) -> cb.like(root.get(field), "%" + value + "%"));
  }
}
