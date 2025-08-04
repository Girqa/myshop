package ru.girqa.paymentservice.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.girqa.paymentservice.model.Payment;

@Service
public class PaymentService {

  public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100000");

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Queue<Payment> paymentRepository = new ConcurrentLinkedQueue<>();
  private final Map<Long, BigDecimal> balanceRepository = new ConcurrentHashMap<>();

  public Mono<BigDecimal> getBalance(Long userId) {
    return Mono.just(balanceRepository.getOrDefault(userId, DEFAULT_AMOUNT))
        .doOnNext(b -> balanceRepository.put(userId, b));
  }

  public Mono<Void> doPayment(Long userId, Long orderId, BigDecimal amount) {
    return getBalance(userId)
        .flatMap(balance -> {
          if (balance.compareTo(amount) < 0) {
            return Mono.error(new IllegalStateException("Not enough money"));
          } else {
            return Mono.just(balanceRepository.merge(userId, amount, BigDecimal::subtract));
          }
        })
        .doOnNext(ignore -> paymentRepository.add(new Payment(userId, orderId, amount)))
        .then();
  }

}
