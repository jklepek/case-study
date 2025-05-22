package com.klepek.product;

import com.klepek.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Product product);
    List<Product> getAllProducts();
    boolean deleteProduct(Long id);
}
