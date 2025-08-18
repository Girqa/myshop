package ru.girqa.myshop.controller;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.client.api.DefaultApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.security.User;
import ru.girqa.payment.dto.BalanceDto;

@Slf4j
@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
public class BalanceController {

  private final DefaultApi paymentGateway;

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public Mono<ResponseEntity<BigDecimal>> balance(
      @AuthenticationPrincipal User user
  ) {
    return paymentGateway.getUserBalance(user.getId())
        .map(BalanceDto::getBalance)
        .map(ResponseEntity::ok)
        .doOnError(throwable -> log.error("Error while getting balance for user {}",
            user.getId(), throwable)
        ).onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
  }

}
