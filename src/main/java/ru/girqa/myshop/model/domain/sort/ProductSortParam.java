package ru.girqa.myshop.model.domain.sort;

import lombok.Getter;

@Getter
public enum ProductSortParam {
  NAME("product_name"),
  PRICE("product_price");

  private final String fieldName;

  ProductSortParam(String fieldName) {
    this.fieldName = fieldName;
  }
}
