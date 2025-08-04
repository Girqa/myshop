package ru.girqa.myshop.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.data.relational.core.query.Update.update;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.BucketProductAmount;
import ru.girqa.myshop.model.domain.Product;

@Repository
@RequiredArgsConstructor
public class BucketRepository {

  private final R2dbcEntityTemplate template;

  @Cacheable("bucket_user_mapping")
  public Mono<Long> getUserIdByBucketId(@NonNull Long bucketId) {
    return template.select(Bucket.class)
        .matching(query(where("id").is(bucketId)))
        .first()
        .map(Bucket::getUserId);
  }

  public Mono<Bucket> findWithFilledProductsByUserId(@NonNull Long userId) {
    return findWithFilledProductsBySomeId("user_id", userId);
  }

  public Mono<Bucket> save(@NonNull Bucket bucket) {
    return template.insert(bucket);
  }

  public Mono<BucketProductAmount> saveProduct(@NonNull BucketProductAmount product) {
    return template.insert(product);
  }

  public Mono<Long> updateProduct(@NonNull BucketProductAmount product) {
    return template.update(BucketProductAmount.class)
        .matching(query(where("bucket_id").is(product.getBucketId())
            .and(where("product_id").is(product.getProductId()))))
        .apply(update("product_amount", product.getAmount()));
  }

  public Mono<Long> deleteProduct(@NonNull Long bucketId, @NonNull Long productId) {
    return template.delete(BucketProductAmount.class)
        .matching(query(
            where("product_id").is(productId)
                .and(where("bucket_id").is(bucketId))
        ))
        .all();
  }

  public Mono<BucketProductAmount> findProduct(
      @NonNull Long bucketId,
      @NonNull Long productId
  ) {
    return template.select(BucketProductAmount.class)
        .matching(query(
            where("bucket_id").is(bucketId)
                .and(where("product_id").is(productId))))
        .one();
  }

  public Mono<Void> deleteProductsByBucketId(@NonNull Long bucketId) {
    return template.delete(BucketProductAmount.class)
        .matching(query(
            where("bucket_id").is(bucketId)
        )).all()
        .then();
  }

  private Mono<Bucket> findWithFilledProductsBySomeId(@NonNull String idColumn, @NonNull Long id) {
    return template.getDatabaseClient().sql("""
                select
                    b.id as bucket_id,
                    b.user_id,
                    bp.product_id,
                    bp.product_amount,
                    p.product_name,
                    p.product_description,
                    p.product_price,
                    p.product_image
                from buckets b
                left join buckets_products bp on bp.bucket_id = b.id
                left join products p on bp.product_id = p.id
                where b.%s = :id
                order by bp.product_id
            """.formatted(idColumn))
        .bind("id", id)
        .map(this::mapBucketWithProduct)
        .all()
        .collectList()
        .flatMap(rows -> {
          if (rows.isEmpty()) {
            return Mono.empty();
          }
          Bucket bucket = rows.getFirst().getT1();
          List<BucketProductAmount> productAmounts = rows.stream()
              .map(Tuple2::getT2)
              .filter(p -> p.getProductId() != null)
              .toList();
          bucket.setProducts(productAmounts);

          return Mono.just(bucket);
        });
  }

  private Tuple2<Bucket, BucketProductAmount> mapBucketWithProduct(Row row, RowMetadata meta) {
    BucketProductAmount productAmount = BucketProductAmount.builder()
        .bucketId(row.get("bucket_id", Long.class))
        .productId(row.get("product_id", Long.class))
        .amount(row.get("product_amount", Integer.class))
        .product(Product.builder()
            .id(row.get("product_id", Long.class))
            .name(row.get("product_name", String.class))
            .description(row.get("product_description", String.class))
            .price(row.get("product_price", BigDecimal.class))
            .imageId(row.get("product_image", Long.class))
            .build())
        .build();

    Bucket bucket = Bucket.builder()
        .id(row.get("bucket_id", Long.class))
        .userId(row.get("user_id", Long.class))
        .build();

    return Tuples.of(bucket, productAmount);
  }
}
