package ru.girqa.myshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.dto.product.CreateProductDto;
import ru.girqa.myshop.model.dto.product.ProductDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  private final BucketService bucketService;

  private final ProductMapper productMapper;

  @GetMapping("/{id}")
  public String getProduct(
      @PathVariable(name = "id") Long productId,
      @RequestParam(name = "usr_id", required = false, defaultValue = "1") Long userId,
      Model model) {
    Product product = productService.findById(productId);
    Bucket bucket = bucketService.findOrCreateByUserId(userId);
    ProductDto productDto = productMapper.toDto(product, bucket);

    model.addAttribute("product", productDto);
    model.addAttribute("bucketId", bucket.getId());

    return "product/product";
  }

  @PostMapping
  public String createProduct(@ModelAttribute CreateProductDto productDto) {
    Product product = productMapper.toDomain(productDto);
    product = productService.save(product);
    return "redirect:/product/%s".formatted(product.getId());
  }

}
