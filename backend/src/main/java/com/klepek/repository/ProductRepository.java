package com.klepek.repository;

import com.klepek.model.StoredProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<StoredProduct, Long> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StoredProduct> findById(Long id);
}
