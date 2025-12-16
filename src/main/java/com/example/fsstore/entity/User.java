// src/main/java/com/example/fsstore/entity/User.java

package com.example.fsstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List; // Thêm import này

@Entity
@Table(name = "users")
@Getter // Thêm Lombok
@Setter // Thêm Lombok
@NoArgsConstructor // Thêm Lombok
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Các trường cơ bản
    private String username;
    private String email;
    private String password;

    // Các trường khác có thể cần thiết cho thông tin giao hàng
    private String firstName;
    private String lastName;
    private String phone;
    private String streetAddress;
    private String city;
    private String zipCode;

    // Thiết lập mối quan hệ với Order (Tùy chọn, nhưng nên có)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

}