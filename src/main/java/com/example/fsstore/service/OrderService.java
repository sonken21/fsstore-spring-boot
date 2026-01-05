package com.example.fsstore.service;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.CartItem;
import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.OrderDetail;
import com.example.fsstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    private static final Double SHIPPING_FEE = 30000.0;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    // --- CÁC PHƯƠNG THỨC MỚI LẤY DỮ LIỆU THỰC TẾ CHO BIỂU ĐỒ ---

    public List<Double> getRevenueDataByDays(int days) {
        List<Double> revenues = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Double dailyRevenue = orderRepository.getTotalRevenueByDate(date);
            revenues.add(dailyRevenue != null ? dailyRevenue : 0.0);
        }
        return revenues;
    }

    public List<Integer> getOrderCountsByDays(int days) {
        List<Integer> counts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long dailyCount = orderRepository.countOrdersByDate(date);
            counts.add(dailyCount != null ? dailyCount.intValue() : 0);
        }
        return counts;
    }

    // --- CÁC PHƯƠNG THỨC ADMIN CŨ ---

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void updateStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng #" + orderId));
    }

    // --- PHƯƠNG THỨC TẠO ĐƠN HÀNG (GIỮ NGUYÊN) ---

    @Transactional
    public Order createOrderFromCart(Long cartId, Order order) {
        Cart cart = cartService.getOrCreateCart(cartId);
        if (cart.getItems().isEmpty() || cart.getTotal() == null || cart.getTotal() <= 0.0) {
            throw new IllegalArgumentException("Giỏ hàng trống.");
        }
        order.setOrderDate(LocalDateTime.now());
        Double subTotal = cart.getTotal();
        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setOrderTotal(subTotal + SHIPPING_FEE);
        order.setStatus("PENDING");

        for (CartItem item : cart.getItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPriceAtPurchase());
            order.addOrderDetail(detail);
        }
        Order savedOrder = orderRepository.save(order);
        cartService.deleteCart(cartId);
        return savedOrder;
    }
}