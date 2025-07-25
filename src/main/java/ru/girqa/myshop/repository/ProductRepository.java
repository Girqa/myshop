package ru.girqa.myshop.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.empty;
import static org.springframework.data.relational.core.query.Query.query;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Product;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

  private final R2dbcEntityTemplate template;

  public Mono<Page<Product>> findAll(@NonNull Pageable page) {
    return template.select(Product.class)
        .matching(query(empty())
            .sort(page.getSort())
            .limit(page.getPageSize())
            .offset(page.getOffset())
        ).all()
        .collectList()
        .zipWith(totalProducts(null))
        .map(t -> new PageImpl<>(t.getT1(), page, t.getT2()));
  }

  public Mono<Page<Product>> findAllByName(@NonNull String name, @NonNull Pageable page) {
    return template.select(Product.class)
        .matching(query(where("product_name").like("%" + name + "%"))
            .sort(page.getSort())
            .limit(page.getPageSize())
            .offset(page.getOffset())
        ).all()
        .collectList()
        .zipWith(totalProducts(name))
        .map(t -> new PageImpl<>(t.getT1(), page, t.getT2()));
  }

  public Mono<Product> findById(@NonNull Long id) {
    return template.select(Product.class)
        .matching(query(where("id").is(id)))
        .one();
  }

  private Mono<Integer> totalProducts(String filteredName) {
    GenericExecuteSpec query = switch (filteredName) {
      case null -> template.getDatabaseClient()
          .sql("select count(*) as pages_amount from products");
      case String name -> template.getDatabaseClient()
          .sql("""
              select count(*) as pages_amount from products
              where product_name like :name
              """)
          .bind("name", "%" + name + "%");
    };

    return query
        .map((row, mapping) -> row.get("pages_amount", Integer.class))
        .one();
  }

  public Mono<Product> save(@NonNull Product product) {
    return template.insert(product);
  }
}
