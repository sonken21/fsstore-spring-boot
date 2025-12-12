// src/main/java/com/example/fsstore/service/ProductService.java

package com.example.fsstore.service;

import com.example.fsstore.entity.Product;
import com.example.fsstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Nên thêm vào nếu sử dụng @Transactional

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // -----------------------------------------------------------
    // PHƯƠNG THỨC MỚI DÀNH CHO CART SERVICE (AN TOÀN BẰNG OPTIONAL)
    // -----------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<Product> findOptionalProductById(Long id) {
        return productRepository.findById(id);
    }

    // -----------------------------------------------------------
    // CÁC PHƯƠNG THỨC HIỆN TẠI KHÁC
    // -----------------------------------------------------------

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product findProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    public List<Product> findTopRatedProductsByGender(String gender, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByGenderOrderByRatingDesc(gender, pageable);
    }

    public Page<Product> findPaginatedProducts(
            Pageable pageable,
            String keyword,
            String filterValue,
            Double minPrice,
            Double maxPrice) {

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasFilter = filterValue != null && !filterValue.isEmpty();
        boolean hasPriceFilter = (minPrice != null) || (maxPrice != null);

        if (hasKeyword || hasFilter || hasPriceFilter) {
            // Giả định ProductRepository có phương thức findFilteredProducts
            return productRepository.findFilteredProducts(
                    keyword,
                    filterValue,
                    minPrice,
                    maxPrice,
                    pageable
            );
        }

        return productRepository.findAll(pageable);
    }
}