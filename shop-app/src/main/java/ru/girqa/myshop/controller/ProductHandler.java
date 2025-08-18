package ru.girqa.myshop.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.girqa.myshop.model.domain.Image;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.domain.security.User;
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

  public Mono<ServerResponse> getProduct(ServerRequest request) {
    Mono<Long> userIdMono = ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication().getPrincipal())
        .cast(User.class)
        .map(User::getId);

    Long productId = Long.valueOf(request.pathVariable("id"));

    Mono<Product> productMono = productService.findById(productId);
    Mono<Bucket> bucketMono = userIdMono.flatMap(bucketService::findFilledOrCreateByUserId);
    Mono<List<String>> authorities = request.principal()
        .cast(Authentication.class)
        .map(authentication -> authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList()
        );

    Mono<String> username = request.principal()
        .map(Principal::getName);

    return Mono.zip(productMono, bucketMono, authorities, username)
        .map(t -> Tuples.of(productMapper.toDto(t.getT1(), t.getT2()), t.getT2(), t.getT3(),
            t.getT4()))
        .flatMap(t -> ServerResponse.ok().render("product/product", Map.of(
            "product", t.getT1(),
            "bucketId", t.getT2().getId(),
            "authorities", t.getT3(),
            "username", t.getT4()
        )));
  }

  @PreAuthorize("hasRole('ADMIN')")
  public Mono<ServerResponse> createProduct(ServerRequest request) {
    Mono<Product> productMono = request.multipartData()
        .flatMapMany(parts -> Flux.fromIterable(parts.asSingleValueMap().entrySet()))
        .map(entry -> Tuples.of(entry.getKey(), DataBufferUtils.join(entry.getValue().content())))
        .flatMap(tuple -> tuple.getT2()
            .map(part -> Tuples.of(tuple.getT1(), new String(readBuffer(part)))))
        .collectList()
        .map(tuples -> tuples.stream()
            .collect(Collectors.toMap(
                Tuple2::getT1,
                Tuple2::getT2
            )))
        .map(parts -> new CreateProductDto(
            parts.get("name"),
            parts.get("description"),
            new BigDecimal(parts.get("price"))
        ))
        .map(productMapper::toDomain);

    Mono<Image> imageMono = request.multipartData()
        .flatMap(parts -> {
          Part imagePartRaw = parts.getFirst("image");
          if (!(imagePartRaw instanceof FilePart imagePart)) {
            return Mono.error(new IllegalArgumentException("Image must be provided"));
          }

          return DataBufferUtils.join(imagePart.content())
              .map(this::readBuffer)
              .map(data -> Image.builder()
                  .data(data)
                  .size((long) data.length)
                  .name(imagePart.filename())
                  .build());
        });

    return Mono.zip(imageMono, productMono).map(t -> {
          t.getT2().setImage(t.getT1());
          return t.getT2();
        })
        .flatMap(productService::save)
        .map(Product::getId)
        .flatMap(id -> ServerResponse.status(HttpStatus.FOUND)
            .header("Location", "/product/%d".formatted(id))
            .build());
  }

  private byte[] readBuffer(DataBuffer dataBuffer) {
    byte[] bytes = new byte[dataBuffer.readableByteCount()];
    dataBuffer.read(bytes);
    DataBufferUtils.release(dataBuffer);
    return bytes;
  }

}
