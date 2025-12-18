package com.example.fsstore.controller;

import com.example.fsstore.entity.User;
import com.example.fsstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private UserService userService; // Tiêm UserService để xử lý logic lưu User

    @GetMapping("/login")
    public String showLoginPage() {
        // Trả về file login.html trong src/main/resources/templates/
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        // 1. Kiểm tra xem username đã tồn tại chưa
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "redirect:/login";
        }

        // 2. Lưu người dùng mới (UserService đã xử lý mã hóa mật khẩu và gán ROLE_USER)
        try {
            userService.registerNewUser(user);
            // 3. Thông báo thành công và chuyển về trang login
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Hãy đăng nhập.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký.");
        }

        return "redirect:/login";
    }
}