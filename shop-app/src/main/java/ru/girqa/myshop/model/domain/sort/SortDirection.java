package ru.girqa.myshop.model.domain.sort;

import lombok.Getter;
import org.springframework.data.domain.Sort.Direction;

@Getter
public enum SortDirection {
  ASC(Direction.ASC),
  DESC(Direction.DESC);

  private final Direction direction;

  SortDirection(Direction direction) {
    this.direction = direction;
  }
}
