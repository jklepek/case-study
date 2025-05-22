package com.klepek.repository;

import com.klepek.model.StoredOrder;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<StoredOrder, Long> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<StoredOrder> findById(@NotNull Long id);
}
