package com.klepek.model;

import java.math.BigDecimal;

public record Product(
        Long id,
        String name,
        Integer quantity,
        BigDecimal pricePerUnit
) {

    // for creating new StoredProduct (no ID yet)
    public Product(
            String name,
            Integer quantity,
            BigDecimal pricePerUnit
    ) {
        this(null, name, quantity, pricePerUnit);
    }

    // for creating new Orders (only ID and quantity)
    public Product(
            Long id,
            Integer quantity
    ) {
        this(id, null, quantity, null);
    }
}
