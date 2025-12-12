package com.example.fsstore.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter; // ⬅️ THÊM LOMBOK
import lombok.Setter; // ⬅️ THÊM LOMBOK
import lombok.NoArgsConstructor; // ⬅️ THÊM LOMBOK
import lombok.ToString; // ⬅️ THÊM LOMBOK
import lombok.EqualsAndHashCode; // ⬅️ THÊM LOMBOK

@Entity
@Table(name = "cart")
@Getter // Tạo getters
@Setter // Tạo setters
@NoArgsConstructor // Thêm constructor mặc định
@ToString(callSuper = false) // Ngăn toString gọi superclass
@EqualsAndHashCode(callSuper = false) // Ngăn equals/hashCode gọi superclass
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thiết lập mối quan hệ OneToMany với CartItem
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // ⭐ FIX LOMBOK: Ngăn toString() truy cập Collection này (gây LLE)
    @EqualsAndHashCode.Exclude // ⭐ FIX LOMBOK: Ngăn equals/hashCode truy cập Collection này
    private List<CartItem> items = new ArrayList<>();

    // ⭐ FIX: Bổ sung phương thức tính TỔNG TIỀN (Thymeleaf gọi là ${cart.total})
    public Double getTotal() {
        if (this.items == null || this.items.isEmpty()) {
            return 0.0;
        }

        // Tính tổng bằng cách gọi getSubTotal() của mỗi CartItem
        return this.items.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
    }

    // Lưu ý: Các phương thức Getter/Setter thủ công của bạn đã bị loại bỏ
    // vì @Getter/@Setter của Lombok đã bao gồm chúng và an toàn hơn.

    // Nếu bạn vẫn muốn các phương thức thủ công (ít cần thiết khi dùng Lombok), bạn có thể thêm lại:
    /*
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    */
}