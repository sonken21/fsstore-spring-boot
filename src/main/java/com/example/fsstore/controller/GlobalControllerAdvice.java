package com.example.fsstore.controller;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute
    public void addCartToModel(HttpSession session, Model model) {
        Long cartId = (Long) session.getAttribute("cartId");
        if (cartId != null) {
            Cart cart = cartService.getOrCreateCart(cartId);
            model.addAttribute("cart", cart);
        }
    }
}