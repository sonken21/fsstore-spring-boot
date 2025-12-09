package com.example.fsstore.repository;

import com.example.fsstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Phương thức này sẽ tìm sản phẩm theo giới tính (Gender)
    // Sắp xếp kết quả theo Rating giảm dần (DESC)
    // Và sử dụng Pageable để chỉ lấy 4 kết quả đầu tiên (TOP 4)
    List<Product> findByGenderOrderByRatingDesc(String gender, Pageable pageable);

    // Phương thức tìm kiếm: Tìm kiếm theo tên (name) chứa chuỗi keyword
    // ILIKE/LIKE (dành cho các dialect SQL khác nhau)
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // PHƯƠNG THỨC MỚI: Lọc theo Keyword VÀ Category (Gender/ProductType)
    @Query("SELECT p FROM Product p WHERE " +
            // Điều kiện 1: Tìm kiếm theo Keyword (nếu keyword không NULL)
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +

            // Điều kiện 2: Lọc theo Category (nếu filterValue không NULL)
            // FilterValue sẽ khớp với cột gender HOẶC cột productType
            "(:filterValue IS NULL OR p.gender = :filterValue OR p.productType = :filterValue)"

            // Sắp xếp phân trang sẽ được áp dụng tự động qua tham số Pageable
    )
    Page<Product> findFilteredProducts(
            @Param("keyword") String keyword,
            @Param("filterValue") String filterValue,
            Pageable pageable
    );
}