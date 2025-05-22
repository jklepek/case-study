package com.klepek.rest;

import com.klepek.model.Order;
import com.klepek.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    @Autowired
    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/pay/{id}")
    public Order payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PutMapping("/cancel/{id}")
    public Order cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}
