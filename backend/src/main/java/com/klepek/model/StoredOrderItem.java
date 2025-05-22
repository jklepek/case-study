package com.klepek.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="order_items")
public class StoredOrderItem {

    public StoredOrderItem() {}

    public StoredOrderItem(
            StoredOrder order,
            StoredProduct product,
            int quantity
    ) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "order_id")
    private StoredOrder order;

    @ManyToOne()
    @JoinColumn(name = "product_id")
    private StoredProduct product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    public Long getId() {
        return id;
    }

    public StoredProduct getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
