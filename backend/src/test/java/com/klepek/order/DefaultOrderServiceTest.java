package com.klepek.order;

import com.klepek.exceptions.InsufficientStockException;
import com.klepek.exceptions.OrderNotFoundException;
import com.klepek.exceptions.OrderExpiredException;
import com.klepek.exceptions.ProductNotFoundException;
import com.klepek.model.*;
import com.klepek.repository.OrderItemsRepository;
import com.klepek.repository.OrdersRepository;
import com.klepek.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DefaultOrderServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    private DefaultOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new DefaultOrderService(ordersRepository, productRepository, orderItemsRepository);
    }

    @Test
    void createOrder_ShouldCreateOrderAndUpdateProductStock() {
        StoredProduct product = new StoredProduct("Test Product", 10, new BigDecimal("99.99"));
        entityManager.persist(product);
        entityManager.flush();

        List<Product> products = new ArrayList<>();
        products.add(new Product(product.getId(), 5));
        Order order = new Order(null, products, OrderStatus.CREATED);

        Order result = orderService.createOrder(order);

        assertThat(result).isNotNull();
        assertThat(result.orderNumber()).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().quantity()).isEqualTo(5);

        StoredOrder storedOrder = entityManager.find(StoredOrder.class, result.orderNumber());
        assertThat(storedOrder).isNotNull();
        assertThat(storedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(storedOrder.getOrderItems()).hasSize(1);
        assertThat(storedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("499.95"));
        assertThat(storedOrder.getExpiresAt()).isAfter(LocalDateTime.now());

        StoredProduct updatedProduct = entityManager.find(StoredProduct.class, product.getId());
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void createOrder_WhenInsufficientStock_ShouldThrowException() {
        StoredProduct product = new StoredProduct("Test Product", 5, new BigDecimal("99.99"));
        entityManager.persist(product);
        entityManager.flush();

        List<Product> products = new ArrayList<>();
        products.add(new Product(product.getId(), 10));
        Order order = new Order(null, products, OrderStatus.CREATED);

        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product: Test Product");
    }

    @Test
    void createOrder_WhenProductNotFound_ShouldThrowException() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(999L, 5));
        Order order = new Order(null, products, OrderStatus.CREATED);

        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found: 999");
    }

    @Test
    void getOrder_WhenOrderExists_ShouldReturnOrder() {
        StoredOrder order = new StoredOrder();
        StoredProduct product = new StoredProduct("Test Product", 10, new BigDecimal("99.99"));
        entityManager.persist(product);
        StoredOrderItem orderItem = new StoredOrderItem(order, product, 5);
        orderItem.setTotalPrice(new BigDecimal("499.95"));
        List<StoredOrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalAmount(new BigDecimal("499.95"));
        entityManager.persist(order);
        entityManager.flush();

        Order result = orderService.getOrder(order.getId());

        assertThat(result).isNotNull();
        assertThat(result.orderNumber()).isEqualTo(order.getId());
        assertThat(result.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().quantity()).isEqualTo(5);
    }

    @Test
    void getOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Order not found: 999");
    }

    @Test
    void payOrder_WhenOrderExists_ShouldUpdateStatus() {
        StoredOrder order = new StoredOrder();
        StoredProduct product = new StoredProduct("Test Product", 10, new BigDecimal("99.99"));
        entityManager.persist(product);
        StoredOrderItem orderItem = new StoredOrderItem(order, product, 5);
        orderItem.setTotalPrice(new BigDecimal("499.95"));
        List<StoredOrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalAmount(new BigDecimal("499.95"));
        entityManager.persist(order);
        entityManager.flush();

        Order result = orderService.payOrder(order.getId());

        assertThat(result).isNotNull();
        assertThat(result.orderNumber()).isEqualTo(order.getId());
        assertThat(result.status()).isEqualTo(OrderStatus.PAID);

        StoredOrder updatedOrder = entityManager.find(StoredOrder.class, order.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void payOrder_WhenOrderExpired_ShouldThrowException() {
        StoredOrder order = new StoredOrder();
        StoredProduct product = new StoredProduct("Test Product", 10, new BigDecimal("99.99"));
        entityManager.persist(product);
        StoredOrderItem orderItem = new StoredOrderItem(order, product, 5);
        orderItem.setTotalPrice(new BigDecimal("499.95"));
        List<StoredOrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalAmount(new BigDecimal("499.95"));
        order.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        entityManager.persist(order);
        entityManager.flush();

        assertThatThrownBy(() -> orderService.payOrder(order.getId()))
                .isInstanceOf(OrderExpiredException.class)
                .hasMessageContaining("Order has expired: " + order.getId());
    }

    @Test
    void payOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        assertThatThrownBy(() -> orderService.payOrder(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found: 999");
    }

    @Test
    void cancelOrder_WhenOrderExists_ShouldUpdateStatusAndRestoreStock() {
        StoredOrder order = new StoredOrder();
        StoredProduct product = new StoredProduct("Test Product", 5, new BigDecimal("99.99"));
        entityManager.persist(product);
        StoredOrderItem orderItem = new StoredOrderItem(order, product, 3);
        orderItem.setTotalPrice(new BigDecimal("299.97"));
        List<StoredOrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalAmount(new BigDecimal("299.97"));
        entityManager.persist(order);
        entityManager.flush();

        Order result = orderService.cancelOrder(order.getId());

        assertThat(result).isNotNull();
        assertThat(result.orderNumber()).isEqualTo(order.getId());
        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);

        StoredOrder updatedOrder = entityManager.find(StoredOrder.class, order.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        StoredProduct updatedProduct = entityManager.find(StoredProduct.class, product.getId());
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void cancelOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        assertThatThrownBy(() -> orderService.cancelOrder(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found: 999");
    }

    @Test
    void checkExpiredOrders_ShouldCancelExpiredOrders() {
        StoredOrder order = new StoredOrder();
        StoredProduct product = new StoredProduct("Test Product", 5, new BigDecimal("99.99"));
        entityManager.persist(product);
        StoredOrderItem orderItem = new StoredOrderItem(order, product, 3);
        orderItem.setTotalPrice(new BigDecimal("299.97"));
        List<StoredOrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalAmount(new BigDecimal("299.97"));
        order.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        entityManager.persist(order);
        entityManager.flush();

        orderService.checkExpiredOrders();

        StoredOrder updatedOrder = entityManager.find(StoredOrder.class, order.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        StoredProduct updatedProduct = entityManager.find(StoredProduct.class, product.getId());
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        StoredOrder order1 = new StoredOrder();
        StoredOrder order2 = new StoredOrder();
        StoredProduct product = new StoredProduct("Test Product", 10, new BigDecimal("99.99"));
        entityManager.persist(product);

        StoredOrderItem orderItem1 = new StoredOrderItem(order1, product, 2);
        orderItem1.setTotalPrice(new BigDecimal("199.98"));
        List<StoredOrderItem> orderItems1 = new ArrayList<>();
        orderItems1.add(orderItem1);
        order1.setOrderItems(orderItems1);
        order1.setTotalAmount(new BigDecimal("199.98"));

        StoredOrderItem orderItem2 = new StoredOrderItem(order2, product, 3);
        orderItem2.setTotalPrice(new BigDecimal("299.97"));
        List<StoredOrderItem> orderItems2 = new ArrayList<>();
        orderItems2.add(orderItem2);
        order2.setOrderItems(orderItems2);
        order2.setTotalAmount(new BigDecimal("299.97"));

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.flush();

        List<Order> results = orderService.getAllOrders();

        assertThat(results).hasSize(2);
        assertThat(results).extracting("status").containsExactly(OrderStatus.CREATED, OrderStatus.CREATED);
        assertThat(results).extracting("products", List.class).allSatisfy(products -> {
            assertThat((List<Product>) products).hasSize(1);
            assertThat(((List<Product>) products).getFirst().name()).isEqualTo("Test Product");
        });
    }
} 
