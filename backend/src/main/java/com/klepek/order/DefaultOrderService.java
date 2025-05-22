package com.klepek.order;

import com.klepek.exceptions.InsufficientStockException;
import com.klepek.exceptions.ProductNotFoundException;
import com.klepek.model.*;
import com.klepek.repository.OrderItemsRepository;
import com.klepek.repository.OrdersRepository;
import com.klepek.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultOrderService implements OrderService {

    private final OrdersRepository ordersRepository;
    private final ProductRepository productsRepository;
    private final OrderItemsRepository orderItemsRepository;

    @Autowired
    public DefaultOrderService(
            OrdersRepository ordersRepository,
            ProductRepository productsRepository,
            OrderItemsRepository orderItemsRepository
    ) {
        this.ordersRepository = ordersRepository;
        this.productsRepository = productsRepository;
        this.orderItemsRepository = orderItemsRepository;
    }

    @Override
    public Order getOrder(Long id) {
        StoredOrder storedOrder = ordersRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Order not found: " + id));
        return new Order(storedOrder.getId(), storedOrder.getOrderItems()
                .stream()
                .map(orderItem -> new Product(
                        orderItem.getProduct().getId(),
                        orderItem.getProduct().getName(),
                        orderItem.getQuantity(),
                        orderItem.getProduct().getPricePerUnit()
                )).collect(Collectors.toList()), storedOrder.getStatus());
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        final StoredOrder storedOrder = new StoredOrder();

        List<StoredOrderItem> orderItems = order.products().stream()
                .map(product -> {
                    StoredProduct storedProduct = productsRepository.findById(product.id())
                            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + product.id()));

                    if (storedProduct.getStockQuantity() < product.quantity()) {
                        throw new InsufficientStockException("Insufficient stock for product: " + product.name());
                    }

                    storedProduct.setStockQuantity(storedProduct.getStockQuantity() - product.quantity());
                    productsRepository.save(storedProduct);

                    StoredOrderItem orderItem = new StoredOrderItem(storedOrder, storedProduct, product.quantity());
                    orderItem.setTotalPrice(storedProduct.getPricePerUnit().multiply(new BigDecimal(product.quantity())));
                    return orderItemsRepository.save(orderItem);
                })
                .collect(Collectors.toList());

        storedOrder.setOrderItems(orderItems);
        BigDecimal totalAmount = orderItems.stream()
                .map(StoredOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        storedOrder.setTotalAmount(totalAmount);
        final StoredOrder savedOrder = ordersRepository.save(storedOrder);

        return new Order(savedOrder.getId(), storedOrder.getOrderItems().stream()
                .map(item -> new Product(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getProduct().getPricePerUnit()
                        )
                )
                .toList(), savedOrder.getStatus()
        );
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long id) {
        StoredOrder order = ordersRepository.getReferenceById(id);
        order.getOrderItems().stream().peek(orderItem -> {
            StoredProduct product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productsRepository.save(product);
        }).forEach(orderItemsRepository::delete);
        order.setStatus(OrderStatus.CANCELLED);
        ordersRepository.save(order);
        return true;
    }

    @Override
    public List<Order> getAllOrders() {
        return ordersRepository.findAll().stream()
                .map(storedOrder -> new Order(storedOrder.getId(), storedOrder.getOrderItems()
                        .stream()
                        .map(orderItem -> new Product(
                                        orderItem.getProduct().getId(),
                                        orderItem.getProduct().getName(),
                                        orderItem.getQuantity(),
                                        orderItem.getProduct().getPricePerUnit()
                                )
                        )
                        .collect(Collectors.toList()), storedOrder.getStatus()))
                .collect(Collectors.toList());
    }
}
