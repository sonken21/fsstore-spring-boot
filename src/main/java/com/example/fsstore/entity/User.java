// src/main/java/com/example/fsstore/entity/User.java

package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter // Thêm Lombok
@Setter // Thêm Lombok
@NoArgsConstructor // Thêm Lombok
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    private String avatar;
    private String role; // "ROLE_USER" hoặc "ROLE_ADMIN"

    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String streetAddress;
    private String city;
    private String zipCode;
    private String resetPasswordToken;
    private LocalDateTime tokenExpiration;

    // Thiết lập mối quan hệ với Order (Tùy chọn, nhưng nên có)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

}