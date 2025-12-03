package com.example.fsstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // <--- THÊM DÒNG NÀY
public class HomeController {
    @GetMapping("/")
    public String homePage() {
        // Chỉ trả về file HTML tĩnh, bỏ qua việc truyền dữ liệu
        return "demo6"; // Tên file HTML trang chủ
    }
}
