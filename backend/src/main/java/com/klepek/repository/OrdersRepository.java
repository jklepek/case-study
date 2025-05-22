package com.klepek.repository;

import com.klepek.model.StoredOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<StoredOrder, Long> {
    @Override
    Optional<StoredOrder> findById(Long id);
}
