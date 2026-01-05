package com.example.fsstore.service;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.CartItem;
import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.OrderDetail;
import com.example.fsstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    // Giữ nguyên phí vận chuyển cố định của bạn
    private static final Double SHIPPING_FEE = 30000.0;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    // ===========================================================
    // PHƯƠNG THỨC MỚI DÀNH CHO ADMIN

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

    // ===========================================================
    // PHƯƠNG THỨC CŨ CỦA BẠN (GIỮ NGUYÊN LOGIC)
    // ===========================================================

    @Transactional
    public Order createOrderFromCart(Long cartId, Order order) {
        Cart cart = cartService.getOrCreateCart(cartId);

        // 1. Kiểm tra giỏ hàng
        if (cart.getItems().isEmpty() || cart.getTotal() == null || cart.getTotal() <= 0.0) {
            throw new IllegalArgumentException("Giỏ hàng trống hoặc tổng tiền bằng 0. Không thể tạo đơn hàng.");
        }

        // 2. Thiết lập thông tin Order
        order.setOrderDate(LocalDateTime.now());
        Double subTotal = cart.getTotal();

        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);

        Double orderTotal = subTotal + SHIPPING_FEE;
        order.setOrderTotal(orderTotal);

        order.setStatus("PENDING");

        // 3. Tạo OrderDetails từ CartItems
        for (CartItem item : cart.getItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPriceAtPurchase()); // Giữ nguyên lấy giá từ CartItem
            order.addOrderDetail(detail);
        }

        // 4. Lưu Order
        Order savedOrder = orderRepository.save(order);

        // 5. Xóa giỏ hàng sau khi đặt hàng thành công
        cartService.deleteCart(cartId);

        return savedOrder;
    }
}