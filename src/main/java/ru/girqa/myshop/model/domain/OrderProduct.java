package ru.girqa.myshop.model.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders_products")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct extends BaseEntity {

  @Size(min = 1)
  @InsertOnlyProperty
  @Column("order_product_name")
  private String name;

  @Size(min = 1)
  @InsertOnlyProperty
  @Column("order_product_description")
  private String description;

  @Positive
  @InsertOnlyProperty
  @Column("order_product_price")
  private BigDecimal price;

  @Positive
  @InsertOnlyProperty
  @Column("order_product_amount")
  private int amount;

  @NotNull
  @InsertOnlyProperty
  @Column("order_product_image")
  private Long imageId;

  @NotNull
  @InsertOnlyProperty
  @Column("order_id")
  private Long orderId;
}
