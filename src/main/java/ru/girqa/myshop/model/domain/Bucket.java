package ru.girqa.myshop.model.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "buckets")
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Bucket extends BaseEntity {

  @Column(name = "user_id", updatable = false, nullable = false)
  private Long userId;

  @Builder.Default
  @ElementCollection
  @CollectionTable(name = "buckets_products", joinColumns = @JoinColumn(name = "bucket_id"))
  private List<ProductAmount> products = new ArrayList<>();

  public List<ProductAmount> getProducts() {
    return Collections.unmodifiableList(products);
  }

  public void clear() {
    products.clear();
  }

  public Optional<Integer> amountOfProduct(Long productId) {
    return products.stream()
        .filter(p -> productId.equals(p.getProduct().getId()))
        .map(ProductAmount::getAmount)
        .findFirst();
  }

  public void increaseProduct(Long id) {
    for (ProductAmount productAmount : products) {
      if (id.equals(productAmount.getProduct().getId())) {
        productAmount.setAmount(productAmount.getAmount() + 1);
      }
    }
  }

  public void decreaseProduct(Long id) {
    for (ProductAmount productAmount : products) {
      if (id.equals(productAmount.getProduct().getId())) {
        int currentAmount = productAmount.getAmount();
        if (currentAmount <= 1) {
          throw new IllegalStateException("Product should be removed when amount is 1 or less");
        }
        productAmount.setAmount(currentAmount - 1);
      }
    }
  }

  public void removeProduct(Long id) {
    products.removeIf(p -> id.equals(p.getProduct().getId()));
  }

  public void addProduct(Product product) {
    products.add(new ProductAmount(product, 1));
  }

  public BigDecimal getTotalPrice() {
    return products.stream()
        .map(p -> BigDecimal.valueOf(p.getAmount()).multiply(p.getProduct().getPrice()))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }
}
