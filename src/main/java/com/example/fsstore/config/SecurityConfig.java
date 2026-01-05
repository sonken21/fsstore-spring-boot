package com.example.fsstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng BCrypt để mã hóa mật khẩu an toàn
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để submit form từ template dễ dàng hơn
                .authorizeHttpRequests(auth -> auth
                        // Các đường dẫn công khai
                        .requestMatchers("/", "/shop/**", "/assets/**", "/product/**", "/login", "/register").permitAll()

                        // Chỉ ADMIN mới vào được các đường dẫn bắt đầu bằng /admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Các trang còn lại (như Checkout, Profile) bắt buộc phải đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")               // Trang hiển thị giao diện đăng nhập
                        .loginProcessingUrl("/login")      // URL xử lý đăng nhập nội bộ

                        // Xử lý chuyển hướng thông minh sau khi đăng nhập thành công
                        .successHandler((request, response, authentication) -> {
                            var roles = authentication.getAuthorities();
                            boolean isAdmin = roles.stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect("/admin/dashboard");
                            } else {
                                response.sendRedirect("/");
                            }
                        })

                        .failureUrl("/login?error=true")   // Thất bại quay lại login kèm tham số báo lỗi
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")              // Đường dẫn để đăng xuất
                        .logoutSuccessUrl("/")             // Đăng xuất xong về trang chủ
                        .invalidateHttpSession(true)       // Xóa session
                        .deleteCookies("JSESSIONID")       // Xóa cookie
                        .permitAll()
                );

        return http.build();
    }
}