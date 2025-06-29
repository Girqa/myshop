package ru.girqa.myshop.model.domain.sort;

import lombok.Getter;
import ru.girqa.myshop.model.domain.Product_;

@Getter
public enum ProductSortParam {
  NAME(Product_.NAME),
  PRICE(Product_.PRICE);

  private final String fieldName;

  ProductSortParam(String fieldName) {
    this.fieldName = fieldName;
  }
}
