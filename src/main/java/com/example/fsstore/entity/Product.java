package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Cần import này
import lombok.ToString; // Cần import này

@Entity
@Table(name = "product")
@Getter // Tạo getters cho tất cả fields
@Setter // Tạo setters cho tất cả fields
@NoArgsConstructor // Thêm constructor không đối số
@AllArgsConstructor // Thêm constructor đầy đủ đối số
// ⭐ FIX LOMBOK: Ngăn Lombok tạo toString/equals/hashCode nguy hiểm (mặc dù Product không có Lazy fields, đây là thói quen tốt)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    private Integer reviewCount;

    @Column(name = "product_type")
    private String productType;

    // Lưu ý: Các phương thức Getter/Setter tùy chỉnh của bạn ở dưới
    // vẫn được giữ nguyên và sẽ ghi đè lên các hàm do Lombok tạo nếu có.

    // Thêm getters và setters cho productType:
    public String getProductType() {
        return productType;
    }
    public void setProductType(String productType) {
        this.productType = productType;
    }

    // Thêm getter và setter cho 'stock'
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        // Đảm bảo loại bỏ khoảng trắng thừa ở đầu và cuối chuỗi
        if (this.imageUrl != null) {
            return this.imageUrl.trim();
        }
        return this.imageUrl;
    }
}