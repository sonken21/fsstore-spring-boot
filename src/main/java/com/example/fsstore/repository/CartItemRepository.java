package com.example.fsstore.repository;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Tìm kiếm một CartItem dựa trên Cart và ID của Product.
     * Đây là truy vấn quan trọng để kiểm tra sản phẩm đã có trong giỏ hàng chưa.
     */
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
}