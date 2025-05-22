package com.klepek.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klepek.model.Order;
import com.klepek.model.OrderStatus;
import com.klepek.model.Product;
import com.klepek.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, 5));
        Order order = new Order(null, products, OrderStatus.CREATED);
        Order createdOrder = new Order(1L, products, OrderStatus.CREATED);

        when(orderService.createOrder(any(Order.class))).thenReturn(createdOrder);

        mockMvc.perform(post("/api/v1/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].quantity").value(5));
    }

    @Test
    void payOrder_ShouldReturnPaidOrder() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, 5));
        Order paidOrder = new Order(1L, products, OrderStatus.PAID);

        when(orderService.payOrder(eq(1L))).thenReturn(paidOrder);

        mockMvc.perform(put("/api/v1/orders/pay/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(1))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void cancelOrder_ShouldReturnCancelledOrder() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, 5));
        Order cancelledOrder = new Order(1L, products, OrderStatus.CANCELLED);

        when(orderService.cancelOrder(eq(1L))).thenReturn(cancelledOrder);

        mockMvc.perform(put("/api/v1/orders/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getOrder_ShouldReturnOrder() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, 5));
        Order order = new Order(1L, products, OrderStatus.CREATED);

        when(orderService.getOrder(eq(1L))).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].quantity").value(5));
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, 5));
        List<Order> orders = List.of(
                new Order(1L, products, OrderStatus.CREATED),
                new Order(2L, products, OrderStatus.PAID)
        );

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[1].orderNumber").value(2))
                .andExpect(jsonPath("$[1].status").value("PAID"));
    }
} 
