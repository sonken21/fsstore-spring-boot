package com.example.fsstore.entity;

import jakarta.persistence.*; // Dùng jakarta.persistence cho Spring Boot 3/4 trở lên
import lombok.Data; // Cần có dependency Lombok

@Entity // <--- Đánh dấu đây là một Entity (ánh xạ với bảng DB)
@Table(name = "product") // <--- Tên bảng trong database
@Data // <--- Annotation của Lombok để tạo Getter/Setter/ToString tự động
public class Product {

    @Id // <--- Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- Tự tăng (Auto Increment)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "price")
    private Double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private Double rating;

    private String gender;
}