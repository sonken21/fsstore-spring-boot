package com.example.fsstore.repository;

import com.example.fsstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm kiếm cho trang Admin (Trả về List)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

    List<Product> findByGenderOrderByRatingDesc(String gender, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    // Lấy top sản phẩm dựa theo Rating giảm dần
    List<Product> findTop10ByOrderByRatingDesc();
    // Lấy sp sắp hết hàng
    List<Product> findByStockLessThanOrderByStockAsc(int threshold);
    // PHƯƠNG THỨC ĐÃ TỐI ƯU: Sử dụng LOWER() cho các điều kiện String
    @Query("SELECT p FROM Product p WHERE " +
            // Điều kiện 1: Tìm kiếm theo Keyword (sử dụng LOWER() cho cả hai)
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +

            // Điều kiện 2: Lọc theo Category/Gender/Type (sử dụng LOWER() cho tất cả String)
            "(:filterValue IS NULL OR LOWER(p.gender) = LOWER(:filterValue) OR LOWER(p.productType) = LOWER(:filterValue)) AND " +

            // Điều kiện 3: Lọc theo Giá Tối thiểu (không thay đổi)
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +

            // Điều kiện 4: Lọc theo Giá Tối đa (không thay đổi)
            "(:maxPrice IS NULL OR p.price <= :maxPrice)"
    )
    Page<Product> findFilteredProducts(
            @Param("keyword") String keyword,
            @Param("filterValue") String filterValue,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );
}