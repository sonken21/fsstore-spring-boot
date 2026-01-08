package com.example.fsstore.service;

import com.example.fsstore.entity.User;
import com.example.fsstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Đăng ký người dùng mới
    public User registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // --- CÁC HÀM MỚI CHO ADMIN ---

    // Lấy toàn bộ danh sách người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Tìm người dùng theo ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + id));
    }

    // Xóa người dùng
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Quên mật khẩu

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}