package com.example.fsstore.repository;

import com.example.fsstore.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Để trống hoặc thêm các phương thức tìm kiếm đặc thù sau
}