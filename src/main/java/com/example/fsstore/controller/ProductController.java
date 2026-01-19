package com.example.fsstore.controller;

import com.example.fsstore.entity.Product;
import com.example.fsstore.entity.User;
import com.example.fsstore.repository.UserRepository;
import com.example.fsstore.repository.WishlistRepository;
import com.example.fsstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class ProductController {

    private final ProductService productService;

    @Autowired
    private WishlistRepository wishlistRepository; // Kiểm tra trạng thái yêu thích từ DB

    @Autowired
    private UserRepository userRepository; // Lấy thông tin User để đối chiếu DB

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Hiển thị trang chi tiết sản phẩm.
     * URL: /product/{id}
     */
    @GetMapping("/product/{id}")
    public String showProductDetails(@PathVariable("id") Long id, Model model) {

        // Tìm sản phẩm theo ID (Sử dụng Service hiện tại của bạn)
        Product product = productService.findProductById(id);

        // Kiểm tra nếu sản phẩm không tồn tại
        if (product == null) {
            return "redirect:/error-404";
        }
        boolean isInWishlist = false;

        // Lấy thông tin định danh của người dùng từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng đã đăng nhập thực sự chưa
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {

            User user = userRepository.findByUsername(auth.getName()).orElse(null);

            if (user != null) {

                isInWishlist = wishlistRepository.findByUserAndProduct(user, product).isPresent();
            }
        }

        // Gửi trạng thái sang HTML:
        model.addAttribute("isInWishlist", isInWishlist);

        // Đặt đối tượng sản phẩm vào Model để hiển thị thông tin
        model.addAttribute("product", product);


        return "product";
    }
}