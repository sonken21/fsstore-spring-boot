package com.example.fsstore.service;

import com.example.fsstore.entity.*;
import com.example.fsstore.repository.OrderRepository;
import com.example.fsstore.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    private static final Double SHIPPING_FEE = 30000.0;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

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

    // --- CÁC PHƯƠNG THỨC ADMIN CẬP NHẬT ---

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void updateStatus(Long orderId, String newStatus) {
        Order order = getOrderById(orderId);
        String oldStatus = order.getStatus();

        // 1. Nếu trạng thái chuyển sang CONFIRMED từ PENDING (Trừ kho)
        if ("CONFIRMED".equalsIgnoreCase(newStatus) && "PENDING".equalsIgnoreCase(oldStatus)) {
            subtractStock(order);
        }

        // 2. Nếu trạng thái chuyển sang CANCELLED từ các trạng thái đã trừ kho (Hoàn kho)
        // Các trạng thái đã trừ kho bao gồm: CONFIRMED, SHIPPED, DELIVERED
        else if ("CANCELLED".equalsIgnoreCase(newStatus) && isAlreadySubtracted(oldStatus)) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng #" + orderId));

        order.getOrderDetails().size();

        return order;
    }

    // --- LOGIC XỬ LÝ KHO HÀNG ---

    private void subtractStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            int quantityNeeded = detail.getQuantity();

            if (product.getStock() < quantityNeeded) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ tồn kho (Cần: " + quantityNeeded + ", Hiện có: " + product.getStock() + ")");
            }

            product.setStock(product.getStock() - quantityNeeded);
            productRepository.save(product);
        }
    }

    private void restoreStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            product.setStock(product.getStock() + detail.getQuantity());
            productRepository.save(product);
        }
    }

    private boolean isAlreadySubtracted(String status) {
        return "CONFIRMED".equalsIgnoreCase(status) || "SHIPPED".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status);
    }

    // --- PHƯƠNG THỨC TẠO ĐƠN HÀNG ---

    @Transactional
    public Order createOrderFromCart(Long cartId, Order order, User user) {
        Cart cart = cartService.getOrCreateCart(cartId);
        if (cart.getItems().isEmpty() || cart.getTotal() == null || cart.getTotal() <= 0.0) {
            throw new IllegalArgumentException("Giỏ hàng trống.");
        }
        order.setUser(user);
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