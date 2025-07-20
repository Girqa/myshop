package ru.girqa.myshop.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
@Getter
@SuperBuilder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

  @InsertOnlyProperty
  @Column("order_price")
  private BigDecimal price;

  @Builder.Default
  @InsertOnlyProperty
  @Column("order_created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Setter
  @Transient
  private List<OrderProduct> products = new ArrayList<>();

}
