package com.klepek.product;

import com.klepek.model.Product;
import com.klepek.model.StoredProduct;
import com.klepek.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DefaultProductServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private DefaultProductService productService;

    @BeforeEach
    void setUp() {
        productService = new DefaultProductService(productRepository);
    }

    @Test
    void createProduct_ShouldCreateAndReturnNewProduct() {
        Product inputProduct = new Product("Test Product", 10, new BigDecimal("99.99"));

        Product result = productService.createProduct(inputProduct);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Test Product");
        assertThat(result.quantity()).isEqualTo(10);
        assertThat(result.pricePerUnit()).isEqualByComparingTo(new BigDecimal("99.99"));

        StoredProduct storedProduct = entityManager.find(StoredProduct.class, result.id());
        assertThat(storedProduct).isNotNull();
        assertThat(storedProduct.getName()).isEqualTo("Test Product");
        assertThat(storedProduct.getStockQuantity()).isEqualTo(10);
        assertThat(storedProduct.getPricePerUnit()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        StoredProduct existingProduct = new StoredProduct("Old Product", 10, new BigDecimal("99.99"));
        entityManager.persist(existingProduct);
        entityManager.flush();

        Product inputProduct = new Product(existingProduct.getId(), "Updated Product", 20, new BigDecimal("149.99"));

        Product result = productService.updateProduct(inputProduct);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(existingProduct.getId());
        assertThat(result.name()).isEqualTo("Updated Product");
        assertThat(result.quantity()).isEqualTo(20);
        assertThat(result.pricePerUnit()).isEqualByComparingTo(new BigDecimal("149.99"));

        StoredProduct updatedProduct = entityManager.find(StoredProduct.class, result.id());
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getName()).isEqualTo("Updated Product");
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(20);
        assertThat(updatedProduct.getPricePerUnit()).isEqualByComparingTo(new BigDecimal("149.99"));
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        Product inputProduct = new Product(999L, "Updated Product", 20, new BigDecimal("149.99"));

        assertThatThrownBy(() -> productService.updateProduct(inputProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found: 999");
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        StoredProduct product1 = new StoredProduct("Product 1", 10, new BigDecimal("99.99"));
        StoredProduct product2 = new StoredProduct("Product 2", 20, new BigDecimal("149.99"));
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.flush();

        List<Product> results = productService.getAllProducts();

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactly("Product 1", "Product 2");
        assertThat(results).extracting("quantity").containsExactly(10, 20);
        assertThat(results).extracting("pricePerUnit")
                .containsExactly(new BigDecimal("99.99"), new BigDecimal("149.99"));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldReturnTrue() {
        StoredProduct product = new StoredProduct("Test Product", 10, new BigDecimal("99.99"));
        entityManager.persist(product);
        entityManager.flush();

        boolean result = productService.deleteProduct(product.getId());

        assertThat(result).isTrue();
        assertThat(entityManager.find(StoredProduct.class, product.getId())).isNull();
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldReturnFalse() {
        boolean result = productService.deleteProduct(999L);

        assertThat(result).isFalse();
    }
} 
