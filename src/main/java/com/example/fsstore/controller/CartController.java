package com.example.fsstore.controller;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private static final String CART_SESSION_KEY = "cartId";
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Lấy hoặc tạo Cart từ Session
        Long cartId = (Long) session.getAttribute(CART_SESSION_KEY);
        Cart cart = cartService.getOrCreateCart(cartId);
        session.setAttribute(CART_SESSION_KEY, cart.getId());

        // 2. Gọi Service để thêm sản phẩm và kiểm tra tồn kho
        boolean success = cartService.addProductToCart(cart, productId, quantity);

        if (!success) {
            // 3. Xử lý khi tồn kho không đủ
            redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm vượt quá tồn kho hiện tại!");
        } else {
            // 4. Xử lý khi thêm thành công
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        }

        // Chuyển hướng về trang chi tiết sản phẩm
        return "redirect:/product/" + productId;
    }

    // Thêm các phương thức khác (view cart, update quantity, remove item...)
}