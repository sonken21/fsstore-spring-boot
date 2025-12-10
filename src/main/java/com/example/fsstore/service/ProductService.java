package com.example.fsstore.service;

import com.example.fsstore.entity.Product;
import com.example.fsstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // (Giữ nguyên các phương thức tiện ích)
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

    // --- PHƯƠNG THỨC CHÍNH ĐÃ SỬA ĐỔI: THÊM LỌC GIÁ ---
    /**
     * Tìm kiếm và phân trang sản phẩm, hỗ trợ tìm kiếm bằng Từ khóa, Giá trị Lọc, VÀ Khoảng Giá.
     * @param pageable Tham số phân trang (page, size, sort)
     * @param keyword Từ khóa tìm kiếm (tên sản phẩm)
     * @param filterValue Giá trị lọc chung ('Nam', 'Nữ', 'Áo', 'Quần',...)
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @return Page<Product>
     */
    public Page<Product> findPaginatedProducts(
            Pageable pageable,
            String keyword,
            String filterValue,
            Double minPrice,
            Double maxPrice) {

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasFilter = filterValue != null && !filterValue.isEmpty();
        boolean hasPriceFilter = (minPrice != null) || (maxPrice != null);

        // Nếu có bất kỳ điều kiện lọc hoặc tìm kiếm nào, gọi phương thức tùy chỉnh
        if (hasKeyword || hasFilter || hasPriceFilter) {
            return productRepository.findFilteredProducts(
                    keyword,
                    filterValue,
                    minPrice,
                    maxPrice,
                    pageable
            );
        }

        // Nếu không có điều kiện lọc nào, trả về tất cả sản phẩm
        return productRepository.findAll(pageable);
    }
}