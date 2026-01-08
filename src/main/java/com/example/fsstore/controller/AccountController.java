package com.example.fsstore.controller;

import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.User;
import com.example.fsstore.service.OrderService;
import com.example.fsstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserDetailsService userDetailsService; // Cần thêm để tải lại thông tin mới

    @GetMapping("/account")
    public String showAccountPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<Order> allOrders = orderService.getOrdersByUser(user);
        List<Order> recentOrders = allOrders.stream().limit(3).collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("totalOrders", allOrders.size());

        return "account";
    }

    @PostMapping("/account/update-profile")
    public String updateProfile(@RequestParam(value = "avatarFile", required = false) MultipartFile file,
                                @RequestParam("username") String newUsername,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
            User user = userService.findByUsername(principal.getName()).get();

            // 1. Kiểm tra nếu đổi tên trùng với người khác (trừ chính mình)
            if (!user.getUsername().equals(newUsername) && userService.existsByUsername(newUsername)) {
                ra.addFlashAttribute("error", "Tên đăng nhập này đã có người sử dụng!");
                return "redirect:/account";
            }

            // 2. Cập nhật thông tin vào DB
            user.setUsername(newUsername);

            if (file != null && !file.isEmpty()) {
                String uploadDir = "src/main/resources/static/assets/img/avatars/";
                String fileName = "user_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
                Path path = Paths.get(uploadDir + fileName);

                if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                user.setAvatar("/assets/img/avatars/" + fileName);
            }

            userService.saveUser(user);

            // 3. QUAN TRỌNG: Cập nhật lại Session của Spring Security
            UserDetails userDetails = userDetailsService.loadUserByUsername(newUsername);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, userDetails.getPassword(), userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/account";
    }

    @GetMapping("/account/order/{id}")
    @Transactional(readOnly = true) // Cực kỳ quan trọng để sửa lỗi 500
    public String viewOrderDetails(@PathVariable("id") Long id, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        try {
            Order order = orderService.getOrderById(id);

            // Kiểm tra bảo mật
            if (!order.getUser().getUsername().equals(principal.getName())) {
                return "redirect:/account";
            }

            model.addAttribute("order", order);
            // Lấy từ Set orderDetails trong Entity Order
            model.addAttribute("orderDetails", order.getOrderDetails());

            return "order-detail";
        } catch (NoSuchElementException e) {
            return "redirect:/account";
        }
    }

    @PostMapping("/account/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long id, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        Order order = orderService.getOrderById(id);

        // Kiểm tra: Chỉ cho phép hủy nếu là đơn của chính họ và đang PENDING
        if (order.getUser().getUsername().equals(principal.getName()) && "PENDING".equals(order.getStatus())) {
            orderService.updateStatus(id, "CANCELLED");
            ra.addFlashAttribute("message", "Đã hủy đơn hàng thành công!");
        } else {
            ra.addFlashAttribute("error", "Không thể hủy đơn hàng này!");
        }

        return "redirect:/account/order/" + id;
    }
}