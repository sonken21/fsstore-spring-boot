package com.example.fsstore.controller;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.Order;
import com.example.fsstore.service.CartService;
import com.example.fsstore.service.OrderService;
import com.example.fsstore.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final VnPayService vnPayService;

    @Autowired
    public OrderController(CartService cartService, OrderService orderService, VnPayService vnPayService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.vnPayService = vnPayService;
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
                             HttpServletRequest request) {

        Long cartId = (Long) session.getAttribute("cartId");
        if (cartId == null) return new RedirectView("/cart");

        try {
            // Bước 1: Tạo đơn hàng từ giỏ hàng (Lưu vào DB)
            Order newOrder = orderService.createOrderFromCart(cartId, order);

            // Bước 2: Kiểm tra phương thức thanh toán
            if ("ONLINE".equals(newOrder.getPaymentMethod())) {
                // Tạo link thanh toán VNPay
                String vnpayUrl = vnPayService.createPaymentUrl(newOrder, request);

                if (vnpayUrl != null && !vnpayUrl.isEmpty()) {
                    // Xóa giỏ hàng vì đơn đã được tạo thành công
                    session.removeAttribute("cartId");
                    return new RedirectView(vnpayUrl);
                }
            }

            // Bước 3: Nếu là COD (Thanh toán khi nhận hàng) hoặc lỗi tạo link VNPay
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
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderIdStr = request.getParameter("vnp_TxnRef");

        // "00" là mã thành công của VNPay
        if ("00".equals(responseCode)) {
            // Tại đây bạn có thể gọi thêm orderService.updateStatus(orderIdStr, "PAID")
            redirectAttributes.addFlashAttribute("success", "Thanh toán qua VNPay thành công!");
            return "redirect:/order/complete/" + orderIdStr;
        } else {
            // Nếu hủy hoặc lỗi, quay về trang checkout và thông báo lỗi
            redirectAttributes.addFlashAttribute("error", "Thanh toán không thành công. Vui lòng thử lại hoặc chọn COD.");
            return "redirect:/order/checkout";
        }
    }

    /**
     * Hiển thị trang hoàn tất đơn hàng
     */
    @GetMapping("/complete/{orderId}")
    public String orderComplete(@PathVariable("orderId") Long orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            return "order-complete"; // Trả về file order-complete.html
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