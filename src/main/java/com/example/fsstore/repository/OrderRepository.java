// src/main/java/com/example/fsstore/repository/OrderRepository.java

package com.example.fsstore.repository;

import com.example.fsstore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}