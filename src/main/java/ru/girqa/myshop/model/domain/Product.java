package ru.girqa.myshop.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

@Entity
@Table(name = "products")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product extends BaseEntity {

  @Size(min = 1)
  @Column(name = "product_name", nullable = false)
  private String name;

  @Size(min = 1)
  @Column(name = "product_description", nullable = false)
  private String description;

  @Positive
  @Column(name = "product_price", nullable = false)
  private BigDecimal price;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_image")
  @ToStringExclude
  private Image image;
}
