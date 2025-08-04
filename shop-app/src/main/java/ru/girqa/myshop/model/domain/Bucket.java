package ru.girqa.myshop.model.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("buckets")
@SuperBuilder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Bucket extends BaseEntity {

  @Getter
  @Column("user_id")
  private Long userId;

  @Setter
  @Transient
  @Builder.Default
  private List<BucketProductAmount> products = new ArrayList<>();

  public List<BucketProductAmount> getProducts() {
    return Collections.unmodifiableList(products);
  }

  public Optional<Integer> amountOfProduct(Long productId) {
    return products.stream()
        .filter(p -> productId.equals(p.getProduct().getId()))
        .map(BucketProductAmount::getAmount)
        .findFirst();
  }

  public BigDecimal getTotalPrice() {
    return products.stream()
        .map(p -> BigDecimal.valueOf(p.getAmount()).multiply(p.getProduct().getPrice()))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }
}
