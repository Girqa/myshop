package ru.girqa.myshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.dto.product.CreateProductDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@Component
@RequiredArgsConstructor
public class ProductHandler {

  private final ProductService productService;

  private final BucketService bucketService;

  private final ProductMapper productMapper;

  private final ObjectMapper objectMapper;

  public Mono<ServerResponse> getProduct(ServerRequest request) {
    Long productId = Long.valueOf(request.pathVariable("id"));
    Long userId = request.queryParam("user_id")
        .map(Long::valueOf)
        .orElse(1L);

    Mono<Product> productMono = productService.findById(productId);
    Mono<Bucket> bucketMono = bucketService.findFilledOrCreateByUserId(userId);

    return Mono.zip(productMono, bucketMono)
        .map(t -> Tuples.of(productMapper.toDto(t.getT1(), t.getT2()), t.getT2()))
        .flatMap(t -> ServerResponse.ok().render("product/product", Map.of(
            "product", t.getT1(),
            "bucketId", t.getT2().getId()
        )));
  }

  public Mono<ServerResponse> createProduct(ServerRequest request) {
    return request.multipartData()
        .flatMap(parts -> {
          CreateProductDto productDto = objectMapper.convertValue(
              parts.toSingleValueMap().entrySet().stream()
                  .filter(e -> e.getValue() instanceof FormFieldPart)
                  .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      e -> ((FormFieldPart) e.getValue()).value()
                  )),
              CreateProductDto.class
          );

          Product product = productMapper.toDomain(productDto);

          Part imagePartRaw = parts.getFirst("image");
          if (!(imagePartRaw instanceof FilePart imagePart)) {
            return Mono.error(new IllegalArgumentException("Image must be provided"));
          }

          Mono<Image> imageMono = DataBufferUtils.join(imagePart.content())
              .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return bytes;
              })
              .map(data -> Image.builder()
                  .data(data)
                  .size((long) data.length)
                  .name(imagePart.filename())
                  .build());

          return imageMono.map(image -> {
                product.setImage(image);
                return product;
              })
              .flatMap(productService::save)
              .map(Product::getId)
              .flatMap(id -> ServerResponse.status(HttpStatus.FOUND)
                  .header("Location", "/product/%d".formatted(id))
                  .build());
        });
  }

}
