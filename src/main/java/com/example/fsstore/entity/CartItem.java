package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name = "cart_items")
@EqualsAndHashCode(exclude = {"cart"}) // Loại bỏ đệ quy vô hạn
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết nhiều CartItem với một Cart (Giỏ hàng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Liên kết CartItem với một Product (Sản phẩm)
    @ManyToOne(fetch = FetchType.EAGER) // EAGER để dễ dàng truy cập product info
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;
}