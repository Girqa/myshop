package ru.girqa.myshop.controller;

import java.util.Comparator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.BucketProductAmount;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;

@Component
@RequiredArgsConstructor
public class BucketHandler {

  private final BucketService bucketService;

  private final ProductMapper productMapper;

  public Mono<ServerResponse> getBucketPage(ServerRequest request) {
    Long userId = request.queryParam("user_id")
        .map(Long::valueOf)
        .orElse(1L);

    Mono<Bucket> bucketMono = bucketService.findFilledOrCreateByUserId(userId);
    Flux<ProductPreviewDto> productsFlux = bucketMono
        .flatMapMany(bucket -> Flux.fromIterable(bucket.getProducts()))
        .map(BucketProductAmount::getProduct)
        .flatMap(product -> bucketMono.map(bucket -> productMapper.toPreview(product, bucket)))
        .sort(Comparator.comparingLong(ProductPreviewDto::id));

    return Mono.zip(bucketMono, productsFlux.collectList())
        .flatMap(t -> ServerResponse.ok()
            .render("bucket/bucket", Map.of(
                "bucketPrice", t.getT1().getTotalPrice(),
                "bucketId", t.getT1().getId(),
                "products", t.getT2()
            )));
  }

  public Mono<ServerResponse> addProduct(ServerRequest request) {
    Long bucketId = Long.valueOf(request.pathVariable("bucket_id"));
    Long productId = Long.valueOf(request.pathVariable("product_id"));
    return bucketService.addProduct(bucketId, productId)
        .then(ServerResponse.accepted().build());
  }

  public Mono<ServerResponse> removeProduct(ServerRequest request) {
    Long bucketId = Long.valueOf(request.pathVariable("bucket_id"));
    Long productId = Long.valueOf(request.pathVariable("product_id"));
    return bucketService.removeProduct(bucketId, productId)
        .then(ServerResponse.accepted().build());
  }

  public Mono<ServerResponse> changeAmount(ServerRequest request) {
    Long bucketId = Long.valueOf(request.pathVariable("bucket_id"));
    Long productId = Long.valueOf(request.pathVariable("product_id"));
    boolean increase = request.queryParam("increase")
        .map(Boolean::valueOf)
        .orElseThrow(() -> new IllegalArgumentException("increase parameter should be provided"));

    if (increase) {
      return bucketService.incrementProductCount(bucketId, productId)
          .then(ServerResponse.accepted().build());
    } else {
      return bucketService.decrementProductCount(bucketId, productId)
          .then(ServerResponse.accepted().build());
    }
  }
}
