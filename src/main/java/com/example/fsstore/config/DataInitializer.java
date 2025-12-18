package com.example.fsstore.config;

import com.example.fsstore.entity.User;
import com.example.fsstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra nếu chưa có tài khoản admin thì mới tạo
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            // Mật khẩu được mã hóa trước khi lưu
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setEmail("admin@fsstore.com");

            userRepository.save(admin);
            System.out.println(">>> Đã tạo tài khoản admin mặc định: admin / admin123");
        }
    }
}