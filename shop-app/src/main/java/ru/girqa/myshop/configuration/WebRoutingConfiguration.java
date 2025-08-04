package ru.girqa.myshop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.girqa.myshop.controller.BucketHandler;
import ru.girqa.myshop.controller.ImageHandler;
import ru.girqa.myshop.controller.MainPageHandler;
import ru.girqa.myshop.controller.OrderHandler;
import ru.girqa.myshop.controller.ProductHandler;

@Configuration
public class WebRoutingConfiguration {

  @Bean
  public RouterFunction<ServerResponse> mainPageHandlerRouterFunction(MainPageHandler handler) {
    return RouterFunctions.route()
        .path("", builder -> builder
            .GET("/", handler::getProductsPage))
        .build();
  }

  @Bean
  public RouterFunction<ServerResponse> productHandlerRouterFunction(ProductHandler handler) {
    return RouterFunctions.route()
        .path("/product", builder -> builder
            .GET("/{id}", handler::getProduct)
            .POST("",
                RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA),
                handler::createProduct)
        ).build();
  }

  @Bean
  public RouterFunction<ServerResponse> bucketHandlerRouterFunction(BucketHandler handler) {
    return RouterFunctions.route()
        .path("/bucket", builder -> builder
            .GET("", handler::getBucketPage)
            .POST("/{bucket_id}/{product_id}", handler::addProduct)
            .DELETE("/{bucket_id}/{product_id}", handler::removeProduct)
            .PUT("/{bucket_id}/{product_id}", handler::changeAmount)
        ).build();
  }

  @Bean
  public RouterFunction<ServerResponse> orderHandlerRouterFunction(OrderHandler handler) {
    return RouterFunctions.route()
        .path("/order", builder -> builder
            .POST("", handler::createOrder)
            .GET("/all", handler::getAllOrders)
            .GET("/{id}", handler::getOrder)
        ).build();
  }

  @Bean
  public RouterFunction<ServerResponse> imageHandlerRouterFunction(ImageHandler handler) {
    return RouterFunctions.route()
        .path("/api/v1/image", builder -> builder
            .GET("/{id}", handler::getImage))
        .build();
  }
}
