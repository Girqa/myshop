package ru.girqa.myshop.controller;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.client.api.DefaultApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.girqa.payment.dto.BalanceDto;

@Slf4j
@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
public class BalanceController {

  private final DefaultApi paymentGateway;

  @GetMapping
  public Mono<ResponseEntity<BigDecimal>> balance(
      @RequestParam(defaultValue = "1", required = false) Long userId
  ) {
    return paymentGateway.getUserBalance(userId)
        .map(BalanceDto::getBalance)
        .map(ResponseEntity::ok)
        .doOnError(throwable -> log.error("Error while getting balance for user {}",
            userId, throwable)
        ).onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
  }

}
