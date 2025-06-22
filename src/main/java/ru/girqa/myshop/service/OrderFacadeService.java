package ru.girqa.myshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myshop.model.domain.Bucket;
import ru.girqa.myshop.model.domain.Order;
import ru.girqa.myshop.service.store.BucketService;
import ru.girqa.myshop.service.store.OrderService;

@Service
@RequiredArgsConstructor
public class OrderFacadeService {

  private final OrderService orderService;

  private final BucketService bucketService;

  @Transactional
  public Order createOrder(@NonNull Long bucketId) {
    Bucket bucket = bucketService.findById(bucketId);
    Order order = orderService.create(bucket);
    bucket.clear();
    return order;
  }

}
