package ru.girqa.myshop.model.domain.search;

import org.springframework.data.jpa.domain.Specification;
import ru.girqa.myshop.model.domain.BaseEntity;

public interface SearchCriteria<T extends BaseEntity> {

  Specification<T> toSpecification();

}
