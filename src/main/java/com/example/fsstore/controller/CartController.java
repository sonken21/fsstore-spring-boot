// src/main/java/com/example/fsstore/controller/CartController.java

package com.example.fsstore.controller;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // ⭐ Cần import List

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 1. Xử lý thêm sản phẩm vào giỏ hàng (POST /cart/add)
    @Transactional
    @PostMapping("/add")
    public String addCartItem(@RequestParam("productId") Long productId,
                              @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        System.out.println(">>> CONTROLLER START: Bắt đầu xử lý thêm CartItem. Product ID nhận được: " + productId);
        Long cartId = (Long) session.getAttribute("cartId");

        try {
            Cart cart = cartService.getOrCreateCart(cartId);
            session.setAttribute("cartId", cart.getId());
            cartService.addProductToCart(cart, productId, quantity);

            redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm vào giỏ hàng thành công!");

            return "redirect:/product/" + productId;

        } catch (IllegalArgumentException e) {
            System.err.println("❌ CONTROLLER CATCH (Sản phẩm không tồn tại): " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/" + productId;
        } catch (Exception e) {
            System.err.println("❌ LỖI HỆ THỐNG TẠI CART CONTROLLER:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi thêm sản phẩm.");
            return "redirect:/product/" + productId;
        }
    }

    // 2. Xử lý hiển thị trang giỏ hàng (GET /cart)
    @Transactional
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Long cartId = (Long) session.getAttribute("cartId");

        Cart cart;
        if (cartId == null) {
            cart = cartService.getOrCreateCart(null);
            session.setAttribute("cartId", cart.getId());
        } else {
            cart = cartService.getOrCreateCart(cartId);
        }

        model.addAttribute("cart", cart);
        return "cart";
    }

    // ⭐ FIX CHÍNH: Thêm phương thức xử lý POST /cart/update-all
    @Transactional
    @PostMapping("/update-all")
    public String updateAllCartItems(@RequestParam("itemId") List<Long> itemIds, // Nhận list ID
                                     @RequestParam("quantity") List<Integer> quantities, // Nhận list Quantity
                                     RedirectAttributes redirectAttributes) {

        if (itemIds.size() != quantities.size()) {
            redirectAttributes.addFlashAttribute("error", "Lỗi dữ liệu: ID và số lượng không khớp.");
            return "redirect:/cart";
        }

        boolean hasError = false;

        // Lặp qua từng cặp ID và số lượng để cập nhật
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            Integer quantity = quantities.get(i);

            try {
                // Sử dụng updateCartItemQuantity đã tạo trong CartService
                cartService.updateCartItemQuantity(itemId, quantity);
            } catch (Exception e) {
                System.err.println("❌ LỖI UPDATE Item ID " + itemId + ": " + e.getMessage());
                hasError = true;
            }
        }

        if (hasError) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật một hoặc nhiều sản phẩm.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Giỏ hàng đã được cập nhật thành công!");
        }

        // ⭐ Lệnh chuyển hướng về /cart sẽ được thực thi
        return "redirect:/cart";
    }

    // ⭐ ĐÃ LOẠI BỎ: Phương thức @PostMapping("/update") cũ

    // 3. Xử lý xóa CartItem (GET /cart/remove/{itemId})
    @Transactional
    @GetMapping("/remove/{itemId}")
    public String removeCartItem(@PathVariable("itemId") Long itemId,
                                 RedirectAttributes redirectAttributes) {

        System.out.println(">>> CONTROLLER REMOVE: Item ID: " + itemId);

        try {
            boolean success = cartService.removeCartItem(itemId);

            if (success) {
                redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm. Item không tồn tại.");
            }

        } catch (Exception e) {
            System.err.println("❌ LỖI HỆ THỐNG KHI XÓA GIỎ HÀNG:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi xóa sản phẩm.");
        }

        return "redirect:/cart";
    }
}