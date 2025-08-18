package ru.girqa.myshop.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.RedisSerializationContextBuilder;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.girqa.myshop.model.domain.bucket.Bucket;
import ru.girqa.myshop.model.domain.product.Product;
import ru.girqa.myshop.model.domain.product.ProductsPage;

@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
      ObjectMapper objectMapper) {
    return builder -> builder.withCacheConfiguration(
            "product_item",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .serializeValuesWith(
                    SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Product.class))
                )
        )
        .withCacheConfiguration(
            "products_page",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(
                    SerializationPair
                        .fromSerializer(
                            new Jackson2JsonRedisSerializer<>(objectMapper, ProductsPage.class))
                )
        )
        .withCacheConfiguration(
            "bucket",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(
                            new Jackson2JsonRedisSerializer<>(objectMapper, Bucket.class))
                )
        );
  }

  @Bean
  public ReactiveRedisTemplate<String, Bucket> redisTemplate(
      ReactiveRedisConnectionFactory factory,
      ObjectMapper objectMapper) {
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<Bucket> valueSerializer = new Jackson2JsonRedisSerializer<>(
        objectMapper, Bucket.class);
    RedisSerializationContextBuilder<String, Bucket> contextBuilder = RedisSerializationContext.newSerializationContext(
        keySerializer);
    RedisSerializationContext<String, Bucket> context = contextBuilder.value(valueSerializer)
        .build();
    return new ReactiveRedisTemplate<>(factory, context);
  }

}
