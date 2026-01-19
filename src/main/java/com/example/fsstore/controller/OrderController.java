package com.example.fsstore.controller;

import com.example.fsstore.config.VnPayConfig;
import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.User;
import com.example.fsstore.service.CartService;
import com.example.fsstore.service.OrderService;
import com.example.fsstore.service.UserService;
import com.example.fsstore.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final VnPayService vnPayService;
    private final UserService userService;
    @Autowired
    public OrderController(CartService cartService, OrderService orderService, VnPayService vnPayService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.vnPayService = vnPayService;
        this.userService = userService;
    }

    /**
     * Hiển thị trang thanh toán
     */
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Long cartId = (Long) session.getAttribute("cartId");
        if (cartId == null) return "redirect:/cart";
        try {
            Cart cart = cartService.getOrCreateCart(cartId);
            model.addAttribute("cart", cart);
            model.addAttribute("order", new Order());
            model.addAttribute("provinces", getVietnameseProvinces());
            return "checkout";
        } catch (Exception e) {
            return "redirect:/cart";
        }
    }

    /**
     * Xử lý đặt hàng (Phân loại COD và ONLINE)
     */
    @PostMapping("/place-order")
    public Object placeOrder(@ModelAttribute("order") Order order,
                             HttpSession session,
                             HttpServletRequest request,
                             Principal principal) {

        Long cartId = (Long) session.getAttribute("cartId");
        if (cartId == null) return new RedirectView("/cart");
        if (principal == null) return new RedirectView("/login");
        try {
            // Lấy đối tượng User từ username của Principal
            String username = principal.getName();
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            // Tạo đơn hàng từ giỏ hàng
            Order newOrder = orderService.createOrderFromCart(cartId, order, currentUser);

            // Kiểm tra phương thức thanh toán
            if ("ONLINE".equals(newOrder.getPaymentMethod())) {
                // Tạo link thanh toán VNPay
                String vnpayUrl = vnPayService.createPaymentUrl(newOrder, request);

                if (vnpayUrl != null && !vnpayUrl.isEmpty()) {
                    // Xóa giỏ hàng vì đơn đã được tạo thành công
                    session.removeAttribute("cartId");
                    return new RedirectView(vnpayUrl);
                }
            }

            // Nếu là COD hoặc lỗi tạo link VNPay
            session.removeAttribute("cartId");
            return new RedirectView("/order/complete/" + newOrder.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("/order/checkout");
        }
    }

    /**
     * Nhận kết quả trả về từ VNPay
     */
    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // 1. Thu thập tham số và tính toán lại Checksum
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash"); // Loại bỏ hash cũ để tính toán hash mới

        // Sắp xếp tham số theo bảng chữ cái
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        // Xây dựng chuỗi hashData
        StringBuilder hashData = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            if (itr.hasNext()) hashData.append('&');
        }

        // 2. Kiểm tra chữ ký
        String signValue = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());

        if (signValue.equals(vnp_SecureHash)) {
            String orderIdStr = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            long vnpAmount = Long.parseLong(request.getParameter("vnp_Amount")) / 100;

            // 3. Kiểm tra chéo với Database
            Order order = orderService.getOrderById(Long.parseLong(orderIdStr));
            if (order != null && order.getOrderTotal().longValue() == vnpAmount) {
                if ("00".equals(responseCode)) {
                    // CẬP NHẬT TRẠNG THÁI ĐÃ THANH TOÁN
                    return "redirect:/order/complete/" + orderIdStr;
                }
            }
        }

        redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ hoặc thanh toán thất bại.");
        return "redirect:/order/checkout";
    }

    /**
     * Hiển thị trang hoàn tất đơn hàng
     */
    @GetMapping("/complete/{orderId}")
    public String orderComplete(@PathVariable("orderId") Long orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            return "order-complete";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    private List<String> getVietnameseProvinces() {
        return Arrays.asList(
                "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "An Giang", "Bà Rịa - Vũng Tàu",
                "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định",
                "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cao Bằng",
                "Cần Thơ", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp",
                "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hải Phòng",
                "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum",
                "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
                "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình",
                "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng",
                "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
                "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long",
                "Vĩnh Phúc", "Yên Bái"
        );
    }
}