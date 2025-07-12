package ru.girqa.myshop.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;
import ru.girqa.myshop.model.dto.product.ProductPageRequestDto;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.model.mapper.ProductPageMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainPageController {

  private final ProductService productService;

  private final ProductPageMapper productPageMapper;

  private final ProductMapper productMapper;

  private final BucketService bucketService;

  /**
   * Retrieve products main page
   *
   * @param requestDto describes page to be retrieved (page number, sorts, filters, elements pre
   *                   page)
   * @param userId     <b>mock user id data to get bucket</b>
   * @return view of products page
   */
  @GetMapping
  public Mono<Rendering> getProductsPage(
      @ModelAttribute ProductPageRequestDto requestDto,
      @RequestParam(name = "user_id", required = false, defaultValue = "1") Long userId) {
    ProductPageRequest pageRequest = productPageMapper.toDomain(requestDto);
    Mono<Page<Product>> pageData = productService.findAllForPage(pageRequest);
    Mono<Bucket> bucket = bucketService.findFilledOrCreateByUserId(userId);

    Mono<List<ProductPreviewDto>> products = pageData.zipWith(bucket)
        .map(t -> productMapper.toPreview(t.getT1().getContent(), t.getT2()));

    return Mono.zip(pageData, bucket, products)
        .map(t -> Rendering.view("product/products")
            .modelAttribute("pageRequest", requestDto)
            .modelAttribute("totalPages", t.getT1().getTotalPages())
            .modelAttribute("bucketId", t.getT2().getId())
            .modelAttribute("products", t.getT3())
            .modelAttribute("availablePageSizes", List.of(10, 20, 50, 100))
            .build());
  }

}
