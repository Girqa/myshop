package ru.girqa.myshop.model.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Product extends BaseEntity {

  @NotBlank
  @Column("product_name")
  private String name;

  @NotBlank
  @Column("product_description")
  private String description;

  @Positive
  @Column("product_price")
  private BigDecimal price;

  @NotNull
  @Column("product_image")
  private Long imageId;

  @Transient
  private Image image;
}
