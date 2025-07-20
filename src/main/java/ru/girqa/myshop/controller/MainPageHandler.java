package ru.girqa.myshop.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.dto.product.ProductPageRequestDto;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.model.mapper.ProductPageMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@Component
@RequiredArgsConstructor
public class MainPageHandler {

  private final ProductService productService;

  private final ProductPageMapper productPageMapper;

  private final ProductMapper productMapper;

  private final BucketService bucketService;

  public Mono<ServerResponse> getProductsPage(ServerRequest request) {
    Long userId = request.queryParam("user_id")
        .map(Long::valueOf)
        .orElse(1L);
    Mono<ProductPageRequestDto> pageRequestDto = request.bind(ProductPageRequestDto.class);

    Mono<Page<Product>> pageData = pageRequestDto.map(productPageMapper::toDomain)
        .flatMap(productService::findAllForPage);

    Mono<Bucket> bucket = bucketService.findFilledOrCreateByUserId(userId);

    Mono<List<ProductPreviewDto>> products = pageData.zipWith(bucket)
        .map(t -> productMapper.toPreview(t.getT1().getContent(), t.getT2()));

    return Mono.zip(pageData, bucket, products, pageRequestDto)
        .flatMap(t -> ServerResponse.ok()
            .render("product/products", Map.of(
                "totalPages", t.getT1().getTotalPages(),
                "bucketId", t.getT2().getId(),
                "products", t.getT3(),
                "pageRequest", t.getT4(),
                "availablePageSizes", List.of(10, 20, 50, 100)
            )));
  }

}
