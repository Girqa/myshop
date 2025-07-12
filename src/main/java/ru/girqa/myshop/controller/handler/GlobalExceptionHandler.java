package ru.girqa.myshop.controller.handler;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public Mono<Rendering> handleException(Exception e, ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    log.error("Handled common error at {}", request.getPath().value(), e);

    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

    return Mono.just(Rendering.view("error/common-error")
        .modelAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR)
        .modelAttribute("message", e.getLocalizedMessage())
        .modelAttribute("timestamp", LocalDateTime.now())
        .build());
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<Rendering> handleException(WebExchangeBindException e, ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    log.error("Handled invalid request error at {}", request.getPath().value(), e);

    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.BAD_REQUEST);

    return Mono.just(Rendering.view("error/common-error")
        .modelAttribute("status", HttpStatus.BAD_REQUEST)
        .modelAttribute("message", "Получен запрос с не валидным телом")
        .modelAttribute("timestamp", LocalDateTime.now())
        .build());
  }

}
