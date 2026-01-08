package com.example.fsstore.controller;

import com.example.fsstore.entity.User;
import com.example.fsstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Hiển thị trang nhập Email
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // 2. Xử lý gửi mã OTP qua Email
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes ra) {
        User user = userService.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Email không tồn tại trong hệ thống!");
            return "redirect:/forgot-password";
        }

        // Tạo mã OTP ngẫu nhiên 6 số
        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));
        user.setResetPasswordToken(otp);
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(5)); // Hết hạn sau 5 phút
        userService.saveUser(user);

        // Gửi Email thực tế
        try {
            sendEmail(email, otp);
            ra.addFlashAttribute("message", "Mã xác nhận đã được gửi đến Email của bạn.");
            ra.addFlashAttribute("email", email);
            return "redirect:/reset-password";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi gửi mail: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    // 3. Hiển thị trang đặt lại mật khẩu
    @GetMapping("/reset-password")
    public String showResetPasswordForm(Model model) {
        if (!model.containsAttribute("email")) {
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    // 4. Xử lý logic đổi mật khẩu mới
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                       @RequestParam String otp,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes ra) {

        User user = userService.findByEmail(email);

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            ra.addFlashAttribute("email", email);
            return "redirect:/reset-password";
        }

        if (user == null || user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(otp)) {
            ra.addFlashAttribute("error", "Mã xác thực không chính xác!");
            ra.addFlashAttribute("email", email);
            return "redirect:/reset-password";
        }

        if (user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("error", "Mã xác thực đã hết hạn!");
            return "redirect:/forgot-password";
        }

        // Cập nhật mật khẩu mới (đã mã hóa)
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpiration(null);
        userService.saveUser(user);

        ra.addFlashAttribute("message", "Đổi mật khẩu thành công! Hãy đăng nhập.");
        return "redirect:/login";
    }

    // Hàm phụ trợ gửi mail
    private void sendEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sonlt10c@gmail.com"); // Email gửi đi
        message.setTo(email);
        message.setSubject("Mã OTP Quên mật khẩu - FS Store");
        message.setText("Mã xác nhận của bạn là: " + otp + ". Mã này sẽ hết hạn trong 5 phút.");
        mailSender.send(message);
    }
}