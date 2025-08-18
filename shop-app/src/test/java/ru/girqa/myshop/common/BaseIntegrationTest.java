package ru.girqa.myshop.common;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.girqa.myshop.common.BaseIntegrationTest.TestSecurityConfiguration;
import ru.girqa.myshop.model.domain.security.User;
import ru.girqa.myshop.model.domain.security.UserRole;
import ru.girqa.myshop.service.store.BucketCacheService;

@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class BaseIntegrationTest {

  protected static final long ADMIN_ID = 11L;

  protected static final String ADMIN_USERNAME = "test_admin";

  protected static final long USER_ID = 32L;

  protected static final String USER_USERNAME = "test_user";

  @Autowired
  protected BucketCacheService bucketCacheService;

  @ServiceConnection
  static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
      "postgres:16-alpine"
  );

  @ServiceConnection
  static final RedisContainer redisContainer = new RedisContainer("redis:7.0.11-alpine");

  @BeforeEach
  void cleanBucketCache() {
    bucketCacheService.delete(USER_ID).block();
  }

  @TestConfiguration
  public static class TestSecurityConfiguration {

    @Bean
    UserDetailsService inMemoryUserDetailsService() {
      return username -> {
        User user = switch (username) {
          case ADMIN_USERNAME -> User.builder()
              .id(ADMIN_ID)
              .username(ADMIN_USERNAME)
              .password(ADMIN_USERNAME)
              .role(UserRole.ADMIN)
              .build();
          case USER_USERNAME -> User.builder()
              .id(USER_ID)
              .username(USER_USERNAME)
              .password(USER_USERNAME)
              .role(UserRole.USER)
              .build();
          default -> {
            System.err.println("Requested unexpected username: " + username);
            yield null;
          }
        };
        System.out.println("Provided test user: " + user);
        return user;
      };
    }
  }

}
