package com.klepek.product;

import com.klepek.model.Product;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Product product);
    boolean deleteProduct(Long id);
}
