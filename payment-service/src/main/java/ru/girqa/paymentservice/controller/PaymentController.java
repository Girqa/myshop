package ru.girqa.paymentservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.girqa.payment.api.DefaultPaymentApi;
import ru.girqa.payment.domain.BalanceDto;
import ru.girqa.payment.domain.PaymentDto;
import ru.girqa.paymentservice.service.PaymentService;

@RestController
public class PaymentController implements DefaultPaymentApi {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @Override
  public Mono<ResponseEntity<String>> doPayment(Long userId, Mono<PaymentDto> paymentDto,
      ServerWebExchange exchange) {
    return paymentDto.flatMap(dto -> paymentService.doPayment(
            userId, dto.getOrderId(), dto.getAmount()
        ))
        .then(Mono.just(ResponseEntity.ok("OK")))
        .onErrorReturn(ResponseEntity.badRequest().body("Can not pay"));
  }

  @Override
  public Mono<ResponseEntity<BalanceDto>> getUserBalance(Long userId,
      ServerWebExchange exchange) {
    return paymentService.getBalance(userId)
        .map(balance -> new BalanceDto(userId, balance))
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
