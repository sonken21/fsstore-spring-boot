package com.example.fsstore.service;

import com.example.fsstore.entity.User;
import com.example.fsstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Đăng ký người dùng mới với vai trò mặc định là ROLE_USER
     */
    public User registerNewUser(User user) {
        // 1. Mã hóa mật khẩu trước khi lưu vào database
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Thiết lập vai trò mặc định (Spring Security yêu cầu có tiền tố ROLE_)
        user.setRole("ROLE_USER");

        // 3. Lưu vào database
        return userRepository.save(user);
    }

    /**
     * Tìm kiếm người dùng theo username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Kiểm tra xem username đã tồn tại hay chưa (dùng cho logic Đăng ký)
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}