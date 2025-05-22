package com.klepek.repository;

import com.klepek.model.StoredOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<StoredOrderItem, Long> {
}
