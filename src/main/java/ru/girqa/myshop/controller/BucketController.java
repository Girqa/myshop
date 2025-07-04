package ru.girqa.myshop.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.ProductAmount;
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
  public String getBucketPage(
      @RequestParam(name = "user_id", required = false, defaultValue = "1") Long userId,
      Model model) {
    Bucket bucket = bucketService.findOrCreateByUserId(userId);
    List<ProductPreviewDto> products = productMapper.toPreview(
        bucket.getProducts().stream()
            .map(ProductAmount::getProduct)
            .toList(),
        bucket);

    model.addAttribute("bucketPrice", bucket.getTotalPrice());
    model.addAttribute("bucketId", bucket.getId());
    model.addAttribute("products", products);

    return "bucket/bucket";
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping("/{bucket_id}/{product_id}")
  public void addProduct(
      @PathVariable("bucket_id") Long bucketId,
      @PathVariable("product_id") Long productId) {
    bucketService.addProduct(bucketId, productId);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  @DeleteMapping("/{bucket_id}/{product_id}")
  public void removeProduct(
      @PathVariable("bucket_id") Long bucketId,
      @PathVariable("product_id") Long productId) {
    bucketService.removeProduct(bucketId, productId);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PutMapping("/{bucket_id}/{product_id}")
  public void changeAmount(
      @PathVariable("bucket_id") Long bucketId,
      @PathVariable("product_id") Long productId,
      @RequestParam("increase") boolean increase
  ) {
    if (increase) {
      bucketService.incrementProductCount(bucketId, productId);
    } else {
      bucketService.decrementProductCount(bucketId, productId);
    }
  }
}
