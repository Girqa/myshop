package ru.girqa.myshop.configuration;

import org.openapitools.client.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.girqa.payment.client.ApiClient;

@Configuration
public class WebClientConfig {

  @Bean
  public DefaultApi apiClient(
      @Value("${app.payment.url}") String basePaymentUrl,
      WebClient.Builder builder,
      ReactiveOAuth2AuthorizedClientManager authManager) {
    WebClient webClient = builder.baseUrl(basePaymentUrl)
        .defaultStatusHandler(HttpStatusCode::isError, ignored -> Mono.empty())
        .filter((request, next) -> authManager.authorize(OAuth2AuthorizeRequest
                .withClientRegistrationId("shop-client")
                .principal("system")
                .build())
            .map(OAuth2AuthorizedClient::getAccessToken)
            .map(token -> ClientRequest.from(request)
                .headers(h -> h.setBearerAuth(token.getTokenValue()))
                .build())
            .flatMap(next::exchange))
        .build();
    ApiClient apiClient = new ApiClient(webClient)
        .setBasePath(basePaymentUrl);
    return new DefaultApi(apiClient);
  }

}
