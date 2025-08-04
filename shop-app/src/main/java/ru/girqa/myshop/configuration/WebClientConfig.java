package ru.girqa.myshop.configuration;

import org.openapitools.client.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.girqa.payment.client.ApiClient;

@Configuration
public class WebClientConfig {

  @Bean
  public DefaultApi apiClient(
      @Value("${app.payment.url}") String basePaymentUrl,
      WebClient.Builder builder) {
    ApiClient client = new ApiClient(builder.baseUrl(basePaymentUrl)
        .defaultStatusHandler(HttpStatusCode::isError, ignored -> Mono.empty())
        .build())
        .setBasePath(basePaymentUrl);
    return new DefaultApi(client);
  }

}
