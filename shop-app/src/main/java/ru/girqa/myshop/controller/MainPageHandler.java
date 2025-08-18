package ru.girqa.myshop.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.product.ProductsPage;
import ru.girqa.myshop.model.domain.security.User;
import ru.girqa.myshop.model.dto.product.ProductPageRequestDto;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;
import ru.girqa.myshop.model.mapper.ProductMapper;
import ru.girqa.myshop.model.mapper.ProductPageMapper;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.ProductService;

@Component
@RequiredArgsConstructor
public class MainPageHandler {

  private final ProductService productService;

  private final ProductPageMapper productPageMapper;

  private final ProductMapper productMapper;

  private final BucketService bucketService;

  public Mono<ServerResponse> getProductsPage(ServerRequest request) {
    Mono<Long> userIdMono = ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication().getPrincipal())
        .cast(User.class)
        .map(User::getId);

    Mono<ProductPageRequestDto> pageRequestDto = request.bind(ProductPageRequestDto.class);

    Mono<ProductsPage> pageData = pageRequestDto.map(productPageMapper::toDomain)
        .flatMap(productService::findAllForPage);

    Mono<Bucket> bucket = userIdMono.flatMap(bucketService::findFilledOrCreateByUserId)
        .cache();

    Mono<List<ProductPreviewDto>> products = pageData.zipWith(bucket)
        .map(t -> productMapper.toPreview(t.getT1().getProducts(), t.getT2()));

    Mono<List<String>> authorities = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList()
        );

    Mono<String> username = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName);

    return Mono.zip(pageData, bucket, products, pageRequestDto, authorities, username)
        .flatMap(t -> ServerResponse.ok()
            .render("product/products", Map.of(
                "totalPages", t.getT1().getTotalPages(),
                "bucketId", t.getT2().getId(),
                "products", t.getT3(),
                "pageRequest", t.getT4(),
                "availablePageSizes", List.of(10, 20, 50, 100),
                "authorities", t.getT5(),
                "username", t.getT6()
            )));

  }

}
