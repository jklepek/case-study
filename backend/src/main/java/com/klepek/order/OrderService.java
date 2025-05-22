package com.klepek.order;

import com.klepek.model.Order;

import java.util.List;

public interface OrderService {

    Order getOrder(Long id);

    Order createOrder(Order order);

    boolean cancelOrder(Long id);

    List<Order> getAllOrders();
}
