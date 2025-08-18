package ru.girqa.myshop.controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.bucket.BucketProductAmount;
import ru.girqa.myshop.model.domain.security.User;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;

@Component
@RequiredArgsConstructor
public class BucketHandler {

  private final BucketService bucketService;

  private final ProductMapper productMapper;

  public Mono<ServerResponse> getBucketPage(ServerRequest ignored) {
    Mono<Long> userIdMono = ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication().getPrincipal())
        .cast(User.class)
        .map(User::getId);

    Mono<Bucket> bucketMono = userIdMono
        .flatMap(bucketService::findFilledOrCreateByUserId);
    Flux<ProductPreviewDto> productsFlux = bucketMono
        .flatMapMany(bucket -> Flux.fromIterable(bucket.getProducts()))
        .map(BucketProductAmount::getProduct)
        .flatMap(product -> bucketMono.map(bucket -> productMapper.toPreview(product, bucket)))
        .sort(Comparator.comparingLong(ProductPreviewDto::id));

    Mono<List<String>> authoritiesMono = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList()
        );

    Mono<String> usernameMono = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName);

    return Mono.zip(bucketMono, productsFlux.collectList(), authoritiesMono, usernameMono)
        .flatMap(t -> ServerResponse.ok()
            .render("bucket/bucket", Map.of(
                "bucketPrice", t.getT1().getTotalPrice(),
                "bucketId", t.getT1().getId(),
                "products", t.getT2(),
                "authorities", t.getT3(),
                "username", t.getT4()
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
