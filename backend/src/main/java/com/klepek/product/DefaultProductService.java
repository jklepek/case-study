package com.klepek.product;

import com.klepek.model.Product;
import com.klepek.model.StoredProduct;
import com.klepek.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;

    public DefaultProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        StoredProduct storedProduct = new StoredProduct(product.name(), product.quantity(), product.pricePerUnit());
        productRepository.save(storedProduct);
        return new Product(storedProduct.getId(), storedProduct.getName(), storedProduct.getStockQuantity(), storedProduct.getPricePerUnit());
    }

    @Override
    public Product updateProduct(Product product) {
        StoredProduct storedProduct = productRepository.findById(product.id())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + product.id()));

        storedProduct.setName(product.name());
        storedProduct.setStockQuantity(product.quantity());
        storedProduct.setPricePerUnit(product.pricePerUnit());

        StoredProduct updatedProduct = productRepository.save(storedProduct);
        return new Product(updatedProduct.getId(), updatedProduct.getName(), updatedProduct.getStockQuantity(), updatedProduct.getPricePerUnit());
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll().stream()
                .map(storedProduct -> new Product(
                        storedProduct.getId(),
                        storedProduct.getName(),
                        storedProduct.getStockQuantity(),
                        storedProduct.getPricePerUnit())
                )
                .toList();
    }

    @Override
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
