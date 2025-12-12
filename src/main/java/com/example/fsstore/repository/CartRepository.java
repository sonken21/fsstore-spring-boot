// src/main/java/com/example/fsstore/repository/CartRepository.java

package com.example.fsstore.repository;

import com.example.fsstore.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Sử dụng @Query để fetch JOIN các CartItem.
    // Điều này giải quyết vấn đề Lazy Loading khi đọc Cart.
    @Query("SELECT c FROM Cart c JOIN FETCH c.items i JOIN FETCH i.product WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);
}