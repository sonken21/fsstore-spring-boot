package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giỏ hàng chứa danh sách các mặt hàng (CartItem)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    // Loại bỏ đệ quy vô hạn và lỗi hashCode/equals
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<CartItem> items = new HashSet<>();

    // Các trường khác (ví dụ: liên kết với User) có thể thêm sau

    // Phương thức tiện ích để tính tổng số lượng sản phẩm
    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}