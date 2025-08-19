package ru.girqa.paymentservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Mono;
import ru.girqa.payment.domain.BalanceDto;
import ru.girqa.payment.domain.PaymentDto;
import ru.girqa.paymentservice.service.PaymentService;

@SpringBootTest
@AutoConfigureWebTestClient
class PaymentControllerTest {

  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  PaymentService paymentServiceMock;

  @Test
  void shouldSuccessfullyGetBalance() {
    BigDecimal BALANCE = new BigDecimal("123.45");
    Long USER_ID = 212L;
    when(paymentServiceMock.getBalance(USER_ID)).thenReturn(Mono.just(BALANCE));

    webTestClient
        .mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_PAYMENT")))
        .get()
        .uri("/api/v1/account/{userId}/balance", USER_ID)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(BalanceDto.class)
        .isEqualTo(new BalanceDto(USER_ID, BALANCE));
  }

  @Test
  void shouldReturnNotFoundOnErroredBalance() {
    Long USER_ID = 421L;
    when(paymentServiceMock.getBalance(USER_ID))
        .thenReturn(Mono.error(new NotFoundException("User not found")));

    webTestClient
        .mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_PAYMENT")))
        .get()
        .uri("/api/v1/account/{userId}/balance", USER_ID)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void shouldSuccessfullyDoPayment() {
    Long USER_ID = 773L;
    PaymentDto PAYMENT_REQUEST = new PaymentDto(4L, BigDecimal.TEN);
    when(paymentServiceMock.doPayment(
        USER_ID, PAYMENT_REQUEST.getOrderId(), PAYMENT_REQUEST.getAmount())
    ).thenReturn(Mono.empty());

    webTestClient
        .mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_PAYMENT")))
        .post()
        .uri("/api/v1/account/{userId}/payment", USER_ID)
        .bodyValue(PAYMENT_REQUEST)
        .accept(MediaType.TEXT_PLAIN)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @Test
  void shouldGetErrorPayment() {
    Long USER_ID = 32L;
    PaymentDto PAYMENT_REQUEST = new PaymentDto(5L, new BigDecimal("66.2"));
    when(paymentServiceMock.doPayment(
        USER_ID, PAYMENT_REQUEST.getOrderId(), PAYMENT_REQUEST.getAmount())
    ).thenReturn(Mono.error(new NotFoundException("User not found")));

    webTestClient
        .mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_PAYMENT")))
        .post()
        .uri("/api/v1/account/{userId}/payment", USER_ID)
        .bodyValue(PAYMENT_REQUEST)
        .accept(MediaType.TEXT_PLAIN)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isBadRequest();
  }
}