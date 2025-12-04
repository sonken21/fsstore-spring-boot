package com.example.fsstore.service;

import com.example.fsstore.entity.Product;
import com.example.fsstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest; // Import cần thiết
import org.springframework.data.domain.Pageable; // Import cần thiết
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // Cần thiết cho findById

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product findProductById(Long id) {
        // Dùng Optional để xử lý trường hợp không tìm thấy
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    // --- PHƯƠNG THỨC MỚI ĐỂ LỌC SẢN PHẨM THEO GIỚI TÍNH VÀ RATING ---
    /**
     * Lấy N sản phẩm có rating cao nhất theo giới tính.
     * Phương thức này gọi phương thức đã định nghĩa trong ProductRepository.
     * * @param gender Giới tính ("Male" hoặc "Female")
     * @param limit Số lượng sản phẩm tối đa muốn lấy (ví dụ: 4)
     * @return Danh sách các sản phẩm được xếp hạng cao nhất theo giới tính
     */
    public List<Product> findTopRatedProductsByGender(String gender, int limit) {
        // Tạo đối tượng Pageable (chỉ lấy trang 0, giới hạn N bản ghi)
        Pageable pageable = PageRequest.of(0, limit);

        // Gọi Repository, kết hợp tìm kiếm theo gender và Pageable
        // Phương thức này phải được định nghĩa trong ProductRepository
        return productRepository.findByGenderOrderByRatingDesc(gender, pageable);
    }
}