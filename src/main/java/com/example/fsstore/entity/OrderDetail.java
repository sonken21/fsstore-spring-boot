// src/main/java/com/example/fsstore/entity/OrderDetail.java

package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- LIÊN KẾT ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // --- THÔNG TIN SẢN PHẨM TẠI THỜI ĐIỂM ĐẶT HÀNG ---

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double subTotal; // subTotal = price * quantity

    // Phương thức tiện ích để tự động tính subTotal
    @PrePersist
    @PreUpdate
    public void calculateSubTotal() {
        if (this.price != null && this.quantity > 0) {
            this.subTotal = this.price * this.quantity;
        } else {
            this.subTotal = 0.0;
        }
    }
}