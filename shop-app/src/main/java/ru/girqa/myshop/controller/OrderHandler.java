package ru.girqa.myshop.controller;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.order.Order;
import ru.girqa.myshop.model.domain.security.User;
import ru.girqa.myshop.model.mapper.OrderMapper;
import ru.girqa.myshop.service.OrderFacadeService;

@Component
@RequiredArgsConstructor
public class OrderHandler {

  private final OrderFacadeService orderFacadeService;

  private final OrderMapper orderMapper;

  public Mono<ServerResponse> createOrder(ServerRequest ignored) {
    Mono<Long> userIdMono = ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication().getPrincipal())
        .cast(User.class)
        .map(User::getId)
        .onErrorReturn(1L);

    return userIdMono.flatMap(orderFacadeService::createOrder)
        .map(Order::getId)
        .map(id -> URI.create("/order/%d".formatted(id)))
        .flatMap(uri -> ServerResponse.status(HttpStatus.FOUND)
            .header("Location", uri.getPath())
            .build());
  }

  public Mono<ServerResponse> getOrder(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));

    Mono<List<String>> authorities = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList()
        );

    Mono<String> username = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName);

    return orderFacadeService.findByIdWithProducts(id)
        .map(orderMapper::toDto)
        .zipWith(Mono.zip(authorities, username))
        .flatMap(t -> ServerResponse.ok().render("order/order", Map.of(
            "order", t.getT1(),
            "authorities", t.getT2().getT1(),
            "username", t.getT2().getT2()
        )));
  }

  public Mono<ServerResponse> getAllOrders(ServerRequest request) {
    Mono<List<String>> authorities = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList()
        );

    Mono<String> username = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName);

    return Mono.zip(orderFacadeService.getHistory(), authorities, username)
        .flatMap(t -> ServerResponse.ok()
            .render("order/orders", Map.of(
                "totalPrice", t.getT1().getTotalPrice(),
                "orders", t.getT1().getOrders().stream()
                    .map(orderMapper::toPreview)
                    .toList(),
                "authorities", t.getT2(),
                "username", t.getT3()
            )));
  }

}
