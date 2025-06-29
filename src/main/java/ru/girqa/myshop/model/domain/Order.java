package ru.girqa.myshop.model.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "orders")
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

  @Column(name = "order_price", nullable = false)
  private BigDecimal price;

  @Builder.Default
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  @JoinColumn(name = "order_id")
  private List<OrderProduct> products = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "order_created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public List<OrderProduct> getProducts() {
    return Collections.unmodifiableList(products);
  }
}
