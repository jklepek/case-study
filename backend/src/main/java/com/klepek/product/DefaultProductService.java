package com.klepek.product;

import com.klepek.model.Product;
import com.klepek.model.StoredProduct;
import com.klepek.model.OrderStatus;
import com.klepek.repository.ProductRepository;
import com.klepek.repository.OrderItemsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;
    private final OrderItemsRepository orderItemsRepository;

    public DefaultProductService(ProductRepository productRepository, OrderItemsRepository orderItemsRepository) {
        this.productRepository = productRepository;
        this.orderItemsRepository = orderItemsRepository;
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
        boolean hasActiveOrders = orderItemsRepository.existsByProductIdAndOrderStatusNot(id, OrderStatus.CANCELLED);

        if (hasActiveOrders) {
            throw new IllegalStateException("Product has active orders and cannot be deleted");
        }

        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
