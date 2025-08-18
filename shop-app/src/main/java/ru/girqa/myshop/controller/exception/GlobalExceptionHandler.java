package ru.girqa.myshop.controller.exception;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.exception.PaymentException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler
{

  private final ViewResolver viewResolver;

  @Override
  public @NonNull Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable e) {
    ServerHttpResponse response = exchange.getResponse();
    return Mono.error(e)
        .doOnError(ex -> {
          ServerHttpRequest request = exchange.getRequest();
          log.error("Handled exception at {}", request.getPath().value(), ex);
        })
        .onErrorResume(WebExchangeBindException.class, ex -> {
              response.setStatusCode(HttpStatus.BAD_REQUEST);
              return viewResolver.resolveViewName("error/common-error", Locale.ENGLISH)
                  .flatMap(view -> view.render(
                      Map.of(
                          "status", HttpStatus.BAD_REQUEST,
                          "message", "Получен запрос с не валидным телом",
                          "timestamp", LocalDateTime.now()),
                      MediaType.TEXT_HTML,
                      exchange
                  ));
            }
        )
        .onErrorResume(PaymentException.class, ignored -> {
              response.setStatusCode(HttpStatus.PAYMENT_REQUIRED);
              return viewResolver.resolveViewName("error/not-enough-money", Locale.ENGLISH)
                  .flatMap(view -> view.render(
                      Map.of("status", HttpStatus.PAYMENT_REQUIRED,
                          "timestamp", LocalDateTime.now()),
                      MediaType.TEXT_HTML,
                      exchange
                  ));
            }
        )
        .onErrorResume(AccessDeniedException.class, ex -> {
              response.setStatusCode(HttpStatus.FORBIDDEN);
              return viewResolver.resolveViewName("error/common-error", Locale.ENGLISH)
                  .flatMap(view -> view.render(
                      Map.of("status", HttpStatus.FORBIDDEN,
                          "message", ex.getMessage(),
                          "timestamp", LocalDateTime.now()),
                      MediaType.TEXT_HTML,
                      exchange
                  ));
            }
        )
        .onErrorResume(Exception.class, ex -> {
              response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
              return viewResolver.resolveViewName("error/common-error", Locale.ENGLISH)
                  .flatMap(view -> view.render(
                      Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR,
                          "message", "Что-то пошло не так: " + ex.getMessage(),
                          "timestamp", LocalDateTime.now()),
                      MediaType.TEXT_HTML,
                      exchange
                  ));
            }
        )
        .cast(Void.class);
  }
}
