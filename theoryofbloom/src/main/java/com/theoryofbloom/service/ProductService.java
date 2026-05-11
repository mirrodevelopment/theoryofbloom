package com.theoryofbloom.service; // IDE trigger

import com.theoryofbloom.model.Product;
import com.theoryofbloom.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) return getAllProducts();
        return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword);
    }

    public List<Product> searchProducts(String keyword, String category, String sort) {
        List<Product> products;
        
        if (keyword != null && !keyword.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword);
        } else {
            products = productRepository.findAll();
        }

        // Filter by category if specified (and not 'All')
        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if ("price_asc".equals(sort)) {
            products.sort((p1, p2) -> {
                java.math.BigDecimal pr1 = p1.getPrice() != null ? p1.getPrice() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal pr2 = p2.getPrice() != null ? p2.getPrice() : java.math.BigDecimal.ZERO;
                return pr1.compareTo(pr2);
            });
        } else if ("price_desc".equals(sort)) {
            products.sort((p1, p2) -> {
                java.math.BigDecimal pr1 = p1.getPrice() != null ? p1.getPrice() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal pr2 = p2.getPrice() != null ? p2.getPrice() : java.math.BigDecimal.ZERO;
                return pr2.compareTo(pr1);
            });
        }

        return products;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}