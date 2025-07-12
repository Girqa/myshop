package ru.girqa.myshop.controller;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.BucketProductAmount;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bucket")
public class BucketController {

  private final BucketService bucketService;

  private final ProductMapper productMapper;

  @GetMapping
  public Mono<Rendering> getBucketPage(
      @RequestParam(name = "user_id", required = false, defaultValue = "1") Long userId) {
    Mono<Bucket> bucketMono = bucketService.findFilledOrCreateByUserId(userId);
    Flux<ProductPreviewDto> productsFlux = bucketMono
        .flatMapMany(bucket -> Flux.fromIterable(bucket.getProducts()))
        .map(BucketProductAmount::getProduct)
        .flatMap(product -> bucketMono.map(bucket -> productMapper.toPreview(product, bucket)))
        .sort(Comparator.comparingLong(ProductPreviewDto::id));

    return bucketMono.zipWith(productsFlux.collectList())
        .map(t -> Rendering.view("bucket/bucket")
            .modelAttribute("bucketPrice", t.getT1().getTotalPrice())
            .modelAttribute("bucketId", t.getT1().getId())
            .modelAttribute("products", t.getT2())
            .build()
        );
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("/{bucket_id}/{product_id}")
  public Mono<Void> addProduct(
      @PathVariable("bucket_id") Long bucketId,
      @PathVariable("product_id") Long productId) {
    return bucketService.addProduct(bucketId, productId);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  @DeleteMapping("/{bucket_id}/{product_id}")
  public Mono<Void> removeProduct(
      @PathVariable("bucket_id") Long bucketId,
      @PathVariable("product_id") Long productId) {
    return bucketService.removeProduct(bucketId, productId);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PutMapping("/{bucket_id}/{product_id}")
  public Mono<Void> changeAmount(
      @PathVariable("bucket_id") Long bucketId,
      @PathVariable("product_id") Long productId,
      @RequestParam("increase") boolean increase
  ) {
    if (increase) {
      return bucketService.incrementProductCount(bucketId, productId);
    } else {
      return bucketService.decrementProductCount(bucketId, productId);
    }
  }
}
