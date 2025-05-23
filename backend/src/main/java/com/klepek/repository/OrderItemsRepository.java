package com.klepek.repository;

import com.klepek.model.StoredOrderItem;
import com.klepek.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<StoredOrderItem, Long> {
    boolean existsByProductIdAndOrderStatusNot(Long productId, OrderStatus status);
}
