package com.example.fsstore.repository;

import com.example.fsstore.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Phương thức này sẽ tìm sản phẩm theo giới tính (Gender)
    // Sắp xếp kết quả theo Rating giảm dần (DESC)
    // Và sử dụng Pageable để chỉ lấy 4 kết quả đầu tiên (TOP 4)
    List<Product> findByGenderOrderByRatingDesc(String gender, Pageable pageable);
}