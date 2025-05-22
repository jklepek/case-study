package com.klepek.model;

import java.util.List;

public record Order(
        Long orderNumber,
        List<Product> products,
        OrderStatus status
) {
}
