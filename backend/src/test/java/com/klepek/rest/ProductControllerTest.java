package com.klepek.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klepek.model.Product;
import com.klepek.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        Product product = new Product("Test Product", 10, new BigDecimal("99.99"));
        Product createdProduct = new Product(1L, "Test Product", 10, new BigDecimal("99.99"));

        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/api/v1/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.pricePerUnit").value(99.99));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        Product product = new Product(1L, "Updated Product", 20, new BigDecimal("149.99"));
        Product updatedProduct = new Product(1L, "Updated Product", 20, new BigDecimal("149.99"));

        when(productService.updateProduct(any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/v1/products/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.pricePerUnit").value(149.99));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldReturnNoContent() throws Exception {
        when(productService.deleteProduct(eq(1L))).thenReturn(true);

        mockMvc.perform(post("/api/v1/products/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(productService.deleteProduct(eq(999L))).thenReturn(false);

        mockMvc.perform(post("/api/v1/products/delete/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Product 1", 10, new BigDecimal("99.99")),
                new Product(2L, "Product 2", 20, new BigDecimal("149.99"))
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].pricePerUnit").value(99.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[1].quantity").value(20))
                .andExpect(jsonPath("$[1].pricePerUnit").value(149.99));
    }
} 
