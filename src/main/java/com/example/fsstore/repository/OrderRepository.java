package com.example.fsstore.repository;

import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    // Lấy tổng doanh thu của một ngày cụ thể
    @Query(value = "SELECT SUM(order_total) FROM orders WHERE CAST(order_date AS DATE) = :date", nativeQuery = true)
    Double getTotalRevenueByDate(@Param("date") LocalDate date);

    // Đếm số đơn hàng của một ngày cụ thể
    @Query(value = "SELECT COUNT(*) FROM orders WHERE CAST(order_date AS DATE) = :date", nativeQuery = true)
    Long countOrdersByDate(@Param("date") LocalDate date);

    // Lấy 10 sản phẩm bán chạy nhất
    @Query(value = "SELECT p.id, p.name, p.image_url, SUM(od.quantity) as totalSales, p.price " +
            "FROM order_detail od " +
            "JOIN product p ON od.product_id = p.id " +
            "GROUP BY p.id, p.name, p.image_url, p.price " +
            "ORDER BY totalSales DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findTop10BestSellers();

}