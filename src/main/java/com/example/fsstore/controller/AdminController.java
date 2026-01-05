package com.example.fsstore.controller;

import com.example.fsstore.entity.Product;
import com.example.fsstore.entity.User;
import com.example.fsstore.service.OrderService;
import com.example.fsstore.service.ProductService;
import com.example.fsstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // ==========================================
    // 1. DASHBOARD TỔNG QUÁT
    // ==========================================
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Bạn có thể thêm thống kê số lượng nếu muốn
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        return "admin/dashboard";
    }

    // ==========================================
    // 2. QUẢN LÝ NGƯỜI DÙNG (USER)
    // ==========================================

    // Danh sách người dùng
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }

    // Chi tiết người dùng
    @GetMapping("/users/detail/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    // Xóa người dùng
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra, Principal principal) {
        User userToDelete = userService.getUserById(id);

        // Ngăn chặn admin tự xóa chính mình
        if (userToDelete.getUsername().equals(principal.getName())) {
            ra.addFlashAttribute("error", "Bạn không thể tự xóa chính mình!");
            return "redirect:/admin/users";
        }

        userService.deleteUser(id);
        ra.addFlashAttribute("message", "Đã xóa người dùng thành công!");
        return "redirect:/admin/users";
    }

    // ==========================================
    // 3. QUẢN LÝ SẢN PHẨM (PRODUCT)
    // ==========================================

    // Danh sách sản phẩm
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/product-list";
    }

    // Form thêm sản phẩm mới
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-add";
    }

    // Xử lý lưu sản phẩm (Thêm mới hoặc Cập nhật)
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes ra) {
        try {
            productService.saveProduct(product, imageFile);
            ra.addFlashAttribute("message", "Đã lưu sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // Xóa sản phẩm
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteProduct(id);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa sản phẩm này!");
        }
        return "redirect:/admin/products";
    }

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders()); // Gọi hàm getAllOrders mới thêm
        return "admin/order-list";
    }

    @GetMapping("/orders/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "admin/order-detail";
    }

    @PostMapping("/orders/update-status")
    public String updateStatus(@RequestParam Long orderId, @RequestParam String status, RedirectAttributes ra) {
        orderService.updateStatus(orderId, status); // Gọi hàm updateStatus mới thêm
        ra.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        return "redirect:/admin/orders";
    }
}