package com.example.fsstore.controller;

import com.example.fsstore.entity.Product;
import com.example.fsstore.entity.User;
import com.example.fsstore.entity.Wishlist;
import com.example.fsstore.repository.ProductRepository;
import com.example.fsstore.repository.UserRepository;
import com.example.fsstore.repository.WishlistRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;


    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String showWishlist(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";

        List<Wishlist> wishlist = wishlistRepository.findByUser(user);
        List<Product> products = wishlist.stream()
                .map(Wishlist::getProduct)
                .collect(Collectors.toList());

        model.addAttribute("wishlistItems", products);
        return "wishlist";
    }

    @GetMapping("/add/{id}")
    @Transactional
    public String toggleWishlist(@PathVariable Long id, HttpServletRequest request) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";

        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            var existing = wishlistRepository.findByUserAndProduct(user, product);
            if (existing.isPresent()) {

                wishlistRepository.deleteByUserAndProduct(user, product);
            } else {

                Wishlist wishlist = new Wishlist();
                wishlist.setUser(user);
                wishlist.setProduct(product);
                wishlistRepository.save(wishlist);
            }
        }

        // Quay lại trang trước đó
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/wishlist");
    }

    @GetMapping("/remove/{id}")
    @Transactional
    public String removeFromWishlist(@PathVariable Long id) {
        User user = getLoggedInUser();
        Product product = productRepository.findById(id).orElse(null);
        if (user != null && product != null) {
            wishlistRepository.deleteByUserAndProduct(user, product);
        }
        return "redirect:/wishlist";
    }
}