package ru.girqa.myshop.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
   * @param model      model to fill data
   * @return view of products page
   */
  @GetMapping
  public String getProductsPage(
      @ModelAttribute ProductPageRequestDto requestDto,
      @RequestParam(name = "user_id", required = false, defaultValue = "1") Long userId,
      Model model) {
    ProductPageRequest pageRequest = productPageMapper.toDomain(requestDto);
    Page<Product> pageData = productService.findAllForPage(pageRequest);
    Bucket bucket = bucketService.findOrCreateByUserId(userId);

    List<ProductPreviewDto> products = productMapper.toPreview(
        pageData.getContent(),
        bucket
    );

    model.addAttribute("products", products);
    model.addAttribute("totalPages", pageData.getTotalPages());
    model.addAttribute("pageRequest", requestDto);
    model.addAttribute("bucketId", bucket.getId());
    model.addAttribute("availablePageSizes", List.of(10, 20, 50, 100));

    return "product/products";
  }

}
