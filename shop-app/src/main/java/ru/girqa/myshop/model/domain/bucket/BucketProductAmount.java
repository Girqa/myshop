package ru.girqa.myshop.model.domain.bucket;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.girqa.myshop.model.domain.product.Product;

@Table("buckets_products")
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BucketProductAmount {

  @NotNull
  @Column("bucket_id")
  private Long bucketId;

  @NotNull
  @Column("product_id")
  private Long productId;

  @Column("product_amount")
  private Integer amount;

  @Transient
  private Product product;

  public void increment() {
    this.amount += 1;
  }

  public void decrement() {
    this.amount -= 1;
  }
}
