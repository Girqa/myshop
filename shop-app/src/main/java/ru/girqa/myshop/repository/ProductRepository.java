package ru.girqa.myshop.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.empty;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.domain.product.ProductPageRequest;
import ru.girqa.myshop.model.domain.product.ProductsPage;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

  private final R2dbcEntityTemplate template;

  public Mono<ProductsPage> findAll(@NonNull ProductPageRequest page) {
    return template.select(Product.class)
        .matching(query(empty())
            .sort(page.getSort())
            .limit(page.getPageSize())
            .offset(page.getOffset())
        ).all()
        .collectList()
        .zipWith(totalProducts(null))
        .map(t -> {
          List<Product> productsPage = t.getT1();
          Integer totalProducts = t.getT2();
          int totalPages = Math.ceilDiv(totalProducts, page.getPageSize());
          return new ProductsPage(productsPage, page, totalPages);
        });
  }

  public Mono<ProductsPage> findAllByName(@NonNull String name, @NonNull ProductPageRequest page) {
    return template.select(Product.class)
        .matching(query(where("product_name").like("%" + name + "%"))
            .sort(page.getSort())
            .limit(page.getPageSize())
            .offset(page.getOffset())
        ).all()
        .collectList()
        .zipWith(totalProducts(name))
        .map(t -> new ProductsPage(t.getT1(), page, t.getT2()));
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
