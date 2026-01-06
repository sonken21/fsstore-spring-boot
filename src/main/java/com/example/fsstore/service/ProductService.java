package com.example.fsstore.service;

import com.example.fsstore.entity.Product;
import com.example.fsstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // Đường dẫn thư mục lưu ảnh (tương đối từ gốc dự án)
    private final String UPLOAD_DIR = "src/main/resources/static/assets/images/demoes/demo6/products/";

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // -----------------------------------------------------------
    // PHƯƠNG THỨC MỚI DÀNH CHO ADMIN
    // -----------------------------------------------------------

    // Lấy tất cả sản phẩm cho danh sách Admin
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Xóa sản phẩm
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Lưu sản phẩm kèm xử lý Upload ảnh
    @Transactional
    public void saveProduct(Product product, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Tạo thư mục nếu chưa tồn tại
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Đổi tên file để tránh trùng lặp (ví dụ: uuid_tenfile.jpg)
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Ghi file vào thư mục vật lý
                Files.write(filePath, imageFile.getBytes());

                // Lưu tên file vào thuộc tính image của Product
                product.setImageUrl(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
            }
        }
        productRepository.save(product);
    }

    // -----------------------------------------------------------
    // CÁC PHƯƠNG THỨC HIỆN TẠI CỦA BẠN (GIỮ NGUYÊN)
    // -----------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<Product> findOptionalProductById(Long id) {
        return productRepository.findById(id);
    }

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

    public List<Product> getTopRatedProducts(int limit) {
        return productRepository.findTop10ByOrderByRatingDesc();
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

    // Cho Admin
    public Page<Product> getAllProductsPaged(int pageNum, int pageSize, String keyword) {
        // Spring Data JPA dùng Index từ 0, nên pageNum - 1
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Sử dụng phương thức có sẵn trong Repository của bạn (trả về Page)
            return productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }

        // Nếu không có keyword, lấy tất cả có phân trang
        return productRepository.findAll(pageable);
    }
}