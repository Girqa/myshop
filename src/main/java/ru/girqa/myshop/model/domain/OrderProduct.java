package ru.girqa.myshop.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "orders_products")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct extends BaseEntity {

  @Size(min = 1)
  @Column(name = "order_product_name", nullable = false, updatable = false)
  private String name;

  @Size(min = 1)
  @Column(name = "order_product_description", nullable = false, updatable = false)
  private String description;

  @Positive
  @Column(name = "order_product_price", nullable = false, updatable = false)
  private BigDecimal price;

  @Positive
  @Column(name = "order_product_amount", nullable = false, updatable = false)
  private int amount;

  @ToStringExclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_product_image", updatable = false)
  private Image image;

  @JoinColumn(name = "order_id", updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Order order;
}
