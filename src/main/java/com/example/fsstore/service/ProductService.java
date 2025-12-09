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

    // Phương thức findPaginatedProducts(Pageable pageable) cũ đã được thay thế
    // bởi phương thức mới có 2 tham số keyword và filterValue.
    // Nếu bạn muốn giữ lại hàm cũ, bạn có thể làm quá tải (overload) phương thức.

    // (Giữ nguyên các phương thức tiện ích)
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product findProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    // --- PHƯƠNG THỨC MỚI ĐỂ LỌC SẢN PHẨM THEO GIỚI TÍNH VÀ RATING ---
    public List<Product> findTopRatedProductsByGender(String gender, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByGenderOrderByRatingDesc(gender, pageable);
    }

    // --- PHƯƠNG THỨC CHÍNH: HỖ TRỢ PHÂN TRANG, TÌM KIẾM VÀ LỌC ---
    /**
     * Tìm kiếm và phân trang sản phẩm, hỗ trợ tìm kiếm bằng Từ khóa VÀ Giá trị Lọc (Category/Gender).
     * @param pageable Tham số phân trang (page, size, sort)
     * @param keyword Từ khóa tìm kiếm (tên sản phẩm)
     * @param filterValue Giá trị lọc chung ('Nam', 'Nữ', 'Áo', 'Quần',...)
     * @return Page<Product>
     */
    public Page<Product> findPaginatedProducts(Pageable pageable, String keyword, String filterValue) {

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasFilter = filterValue != null && !filterValue.isEmpty();

        // Nếu có bất kỳ điều kiện lọc hoặc tìm kiếm nào, gọi phương thức tùy chỉnh
        if (hasKeyword || hasFilter) {
            // SỬ DỤNG PHƯƠNG THỨC @Query MỚI TỪ REPOSITORY
            return productRepository.findFilteredProducts(
                    keyword,
                    filterValue,
                    pageable
            );
        }

        // Nếu không có điều kiện lọc nào, trả về tất cả sản phẩm
        // Lưu ý: Nếu có phân trang (page, size) thì phân trang vẫn được áp dụng
        return productRepository.findAll(pageable);
    }
}