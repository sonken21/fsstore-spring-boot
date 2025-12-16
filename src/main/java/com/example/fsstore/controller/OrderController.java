// src/main/java/com/example/fsstore/controller/OrderController.java

package com.example.fsstore.controller;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.Order; // Đảm bảo entity này đúng
import com.example.fsstore.service.CartService;
import com.example.fsstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    @Autowired
    public OrderController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // Danh sách Tỉnh/Thành phố cơ bản Việt Nam (có thể được cải tiến bằng cách lấy từ DB/Service)
    private List<String> getVietnameseProvinces() {
        return Arrays.asList(
                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
                "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
                "Bình Thuận", "Cà Mau", "Cao Bằng", "Cần Thơ", "Đà Nẵng",
                "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp",
                "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội", "Hà Tĩnh",
                "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", "Hưng Yên",
                "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng",
                "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An",
                "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình",
                "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng",
                "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
                "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long",
                "Vĩnh Phúc", "Yên Bái"
        );
    }

    // 1. Xử lý hiển thị trang Checkout (GET /order/checkout)
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long cartId = (Long) session.getAttribute("cartId");

        if (cartId == null) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/cart";
        }

        try {
            Cart cart = cartService.getOrCreateCart(cartId);

            if (cart.getItems().isEmpty() || cart.getTotal() == null || cart.getTotal() <= 0.0) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng rỗng, vui lòng thêm sản phẩm.");
                return "redirect:/cart";
            }

            model.addAttribute("cart", cart);
            model.addAttribute("order", new Order());

            // THÊM DANH SÁCH TỈNH THÀNH VÀO MODEL CHO THẺ <select>
            model.addAttribute("provinces", getVietnameseProvinces());

            return "checkout";
        } catch (Exception e) {
            session.removeAttribute("cartId");
            redirectAttributes.addFlashAttribute("error", "Lỗi: Giỏ hàng không hợp lệ.");
            return "redirect:/cart";
        }
    }

    // 2. Xử lý đặt hàng (POST /order/place-order)
    @PostMapping("/place-order")
    public String placeOrder(@ModelAttribute("order") Order order,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Long cartId = (Long) session.getAttribute("cartId");

        if (cartId == null) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống.");
            return "redirect:/cart";
        }

        try {
            // Kiểm tra Validation sơ bộ (có thể dùng @Valid cho validation chi tiết hơn)
            if (order.getFirstName() == null || order.getFirstName().isEmpty() ||
                    order.getStreetAddress() == null || order.getStreetAddress().isEmpty() ||
                    order.getPhone() == null || order.getPhone().isEmpty() ||
                    order.getCity() == null || order.getCity().isEmpty()) {

                redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ các trường bắt buộc.");
                // Dùng addFlashAttribute để giữ lại dữ liệu form nếu có thể, hoặc chuyển hướng lại trang checkout.
                return "redirect:/order/checkout";
            }

            Order newOrder = orderService.createOrderFromCart(cartId, order);

            session.removeAttribute("cartId");

            redirectAttributes.addFlashAttribute("success", "Đơn hàng của bạn đã được đặt thành công!");

            return "redirect:/order/complete/" + newOrder.getId();

        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/order/checkout";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi không xác định khi xử lý đơn hàng.");
            return "redirect:/order/checkout";
        }
    }

    // 3. Phương thức xử lý trang Order Complete
    @GetMapping("/complete/{orderId}")
    public String orderComplete(@PathVariable("orderId") Long orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);

            return "order-complete";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/";
        }
    }
}