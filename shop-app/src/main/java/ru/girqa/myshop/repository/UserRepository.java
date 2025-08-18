package ru.girqa.myshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.girqa.myshop.model.domain.security.User;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

  Mono<User> findByUsername(String username);

}
