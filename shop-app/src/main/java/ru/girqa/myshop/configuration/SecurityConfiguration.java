package ru.girqa.myshop.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CorsSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.session.InMemoryReactiveSessionRegistry;
import org.springframework.security.core.session.ReactiveSessionRegistry;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.SessionLimit;
import ru.girqa.myshop.controller.exception.GlobalExceptionHandler;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ReactiveAuthenticationManager authenticationManager,
      GlobalExceptionHandler globalExceptionHandler
  ) {
    return http
        .authenticationManager(authenticationManager)
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/bucket/**", "/api/v1/balance", "/order/**").hasRole("USER")
            .pathMatchers("/", "/login", "/logout", "/webjars/**", "/static/**", "/product",
                "/product/*", "/api/v1/image/*").hasAnyRole("ANONYMOUS", "USER", "ADMIN"))
        .anonymous(anonymous -> anonymous
            .principal("anonymousUser")
            .authorities("ROLE_ANONYMOUS")
            .key("anonymousKey"))
        .formLogin(Customizer.withDefaults())
        .csrf(CsrfSpec::disable)
        .cors(CorsSpec::disable)
        .sessionManagement(sessionManagement -> sessionManagement
            .concurrentSessions(concurrentSessions -> concurrentSessions
                .maximumSessions(SessionLimit.of(1))
                .sessionRegistry(reactiveSessionRegistry())))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedHandler(globalExceptionHandler::handle))
        .build();
  }

  @Bean
  public ReactiveSessionRegistry reactiveSessionRegistry() {
    return new InMemoryReactiveSessionRegistry();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ReactiveAuthenticationManager reactiveAuthenticationManager(
      ReactiveUserDetailsService reactiveUserDetailsService
  ) {
    var manager = new UserDetailsRepositoryReactiveAuthenticationManager(
        reactiveUserDetailsService);

    manager.setPasswordEncoder(passwordEncoder());
    return manager;
  }

}
