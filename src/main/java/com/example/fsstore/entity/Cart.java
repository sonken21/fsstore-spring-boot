package com.example.fsstore.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "cart")
@Getter // Tạo getters
@Setter // Tạo setters
@NoArgsConstructor // Thêm constructor mặc định
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thiết lập mối quan hệ OneToMany với CartItem
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // ⭐ FIX LOMBOK: Ngăn toString() truy cập Collection này (gây LLE)
    @EqualsAndHashCode.Exclude // ⭐ FIX LOMBOK: Ngăn equals/hashCode truy cập Collection này
    private List<CartItem> items = new ArrayList<>();

    // Bổ sung phương thức tính TỔNG TIỀN
    public Double getTotal() {
        if (this.items == null || this.items.isEmpty()) {
            return 0.0;
        }

        // Tính tổng bằng cách gọi getSubTotal() của mỗi CartItem
        return this.items.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
    }


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