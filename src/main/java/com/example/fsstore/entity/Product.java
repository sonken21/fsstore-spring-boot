package com.example.fsstore.entity;

import jakarta.persistence.*; // Dùng jakarta.persistence cho Spring Boot 3/4 trở lên
import lombok.Data; // Cần có dependency Lombok

@Entity // <--- Đánh dấu đây là một Entity (ánh xạ với bảng DB)
@Table(name = "product") // <--- Tên bảng trong database
@Data // <--- Annotation của Lombok để tạo Getter/Setter/ToString tự động
public class Product {

    @Id // <--- Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- Tự tăng (Auto Increment)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "price")
    private Double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private Double rating;

    private String gender;

    private Integer reviewCount;

    @Column(name = "product_type")
    private String productType;
    // ... constructors, getters và setters cho các thuộc tính hiện tại ...

    // Thêm getters và setters cho productType:

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getImageUrl() {
        // Đảm bảo loại bỏ khoảng trắng thừa ở đầu và cuối chuỗi
        if (this.imageUrl != null) {
            return this.imageUrl.trim();
        }
        return this.imageUrl;
    }
}