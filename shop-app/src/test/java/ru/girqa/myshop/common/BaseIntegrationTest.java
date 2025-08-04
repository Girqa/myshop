package ru.girqa.myshop.common;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class BaseIntegrationTest {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
      "postgres:16-alpine"
  );

  @Container
  @ServiceConnection
  static final RedisContainer redisContainer = new RedisContainer("redis:7.0.11-alpine");

}
