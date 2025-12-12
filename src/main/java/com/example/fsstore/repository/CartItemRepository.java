// src/main/java/com/example/fsstore/repository/CartItemRepository.java

package com.example.fsstore.repository;

import com.example.fsstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Tìm CartItem dựa trên Cart ID và Product ID
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}