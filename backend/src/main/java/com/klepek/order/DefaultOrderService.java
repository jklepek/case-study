package com.klepek.order;

import com.klepek.exceptions.InsufficientStockException;
import com.klepek.exceptions.OrderNotFoundException;
import com.klepek.exceptions.OrderExpiredException;
import com.klepek.exceptions.ProductNotFoundException;
import com.klepek.model.*;
import com.klepek.repository.OrderItemsRepository;
import com.klepek.repository.OrdersRepository;
import com.klepek.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
        return new Order(storedOrder.getId(), mapOrderItemsToProducts(storedOrder), storedOrder.getStatus());
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
                        throw new InsufficientStockException("Insufficient stock for product: " + storedProduct.getName());
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

        return new Order(savedOrder.getId(), mapOrderItemsToProducts(storedOrder), savedOrder.getStatus());
    }

    @Override
    @Transactional
    public Order payOrder(Long id) {
        StoredOrder storedOrder = ordersRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        if (storedOrder.isExpired()) {
            throw new OrderExpiredException("Order has expired: " + id);
        }

        if (storedOrder.getStatus() == OrderStatus.PAID) {
            return new Order(storedOrder.getId(), mapOrderItemsToProducts(storedOrder), storedOrder.getStatus());
        } else {
            storedOrder.setStatus(OrderStatus.PAID);
            StoredOrder paidOrder = ordersRepository.save(storedOrder);
            return new Order(paidOrder.getId(), mapOrderItemsToProducts(paidOrder), paidOrder.getStatus());
        }
    }

    @Override
    @Transactional
    public Order cancelOrder(Long id) {
        StoredOrder order = ordersRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return new Order(order.getId(), mapOrderItemsToProducts(order), order.getStatus());
        }

        for (StoredOrderItem orderItem : order.getOrderItems()) {
            StoredProduct product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productsRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        StoredOrder cancelledOrder = ordersRepository.save(order);
        return new Order(cancelledOrder.getId(), mapOrderItemsToProducts(cancelledOrder), cancelledOrder.getStatus());
    }

    @Override
    public List<Order> getAllOrders() {
        return ordersRepository.findAll().stream()
                .map(storedOrder -> new Order(storedOrder.getId(), mapOrderItemsToProducts(storedOrder), storedOrder.getStatus()))
                .toList();
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredOrders() {
        List<StoredOrder> expiredOrders = ordersRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.CREATED && order.isExpired())
                .toList();

        for (StoredOrder order : expiredOrders) {
            cancelOrder(order.getId());
        }
    }

    private List<Product> mapOrderItemsToProducts(StoredOrder order) {
        return order.getOrderItems()
                .stream()
                .map(orderItem -> new Product(
                        orderItem.getProduct().getId(),
                        orderItem.getProduct().getName(),
                        orderItem.getQuantity(),
                        orderItem.getProduct().getPricePerUnit()
                )).toList();
    }
}
