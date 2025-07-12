package ru.girqa.myshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.dto.product.CreateProductDto;
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
  public Mono<Rendering> getProduct(
      @PathVariable(name = "id") Long productId,
      @RequestParam(name = "usr_id", required = false, defaultValue = "1") Long userId) {
    Mono<Product> productMono = productService.findById(productId);
    Mono<Bucket> bucketMono = bucketService.findFilledOrCreateByUserId(userId);

    return Mono.zip(productMono, bucketMono)
        .map(t -> Tuples.of(productMapper.toDto(t.getT1(), t.getT2()), t.getT2()))
        .map(t -> Rendering.view("product/product")
            .modelAttribute("product", t.getT1())
            .modelAttribute("bucketId", t.getT2().getId())
            .build()
        );
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<String> createProduct(
      @ModelAttribute Mono<CreateProductDto> productDto,
      @RequestPart("image") FilePart imageFile) {

    Mono<Product> productMono = productDto.map(productMapper::toDomain);

    Mono<Image> image = DataBufferUtils.join(imageFile.content())
        .map(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);
          DataBufferUtils.release(dataBuffer);
          return bytes;
        })
        .map(data -> Image.builder()
            .data(data)
            .size((long) data.length)
            .name(imageFile.filename())
            .build());

    return productMono.zipWith(image)
        .map(t -> {
          t.getT1().setImage(t.getT2());
          return t.getT1();
        })
        .flatMap(productService::save)
        .map(savedProduct -> "redirect:/product/%s".formatted(savedProduct.getId()));
  }

}
