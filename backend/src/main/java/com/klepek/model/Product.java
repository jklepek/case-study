package com.klepek.model;

import java.math.BigDecimal;

public record Product(
        Long id,
        String name,
        Integer quantity,
        BigDecimal pricePerUnit
) {

    public Product(
            String name,
            Integer quantity,
            BigDecimal pricePerUnit
    ) {
        this(null, name, quantity, pricePerUnit);
    }

    public Product(
            Long id,
            Integer quantity
    ) {
        this(id, null, quantity, null);
    }
}
