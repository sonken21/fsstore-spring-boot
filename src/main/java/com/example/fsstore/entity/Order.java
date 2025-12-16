// src/main/java/com/example/fsstore/entity/Order.java

package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- THÔNG TIN NGƯỜI NHẬN HÀNG ---
    private String firstName;
    private String lastName;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String streetAddress;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String email;
    @Column(columnDefinition = "TEXT")
    private String orderNotes;

    // --- TÍNH TOÁN VÀ TRẠNG THÁI (Dùng Double) ---

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private Double subTotal; // Tổng tiền hàng

    private Double shippingFee = 0.0; // Phí vận chuyển

    @Column(nullable = false)
    private Double orderTotal; // Tổng tiền cuối cùng

    private LocalDateTime orderDate = LocalDateTime.now();

    // --- LIÊN KẾT ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Giả sử Entity User có tồn tại

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> orderDetails = new HashSet<>();

    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }
}