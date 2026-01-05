package com.example.fsstore.controller;

import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.Product;
import com.example.fsstore.entity.User;
import com.example.fsstore.repository.OrderRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;
    // ==========================================
    // 1. DASHBOARD TỔNG QUÁT (CẬP NHẬT BIỂU ĐỒ)
    // ==========================================
    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(name = "days", defaultValue = "7") int days, Model model) {
        try {
            // 1. Thống kê tổng quát
            List<Order> allOrders = orderService.getAllOrders();
            model.addAttribute("totalUsers", userService.getAllUsers().size());
            model.addAttribute("totalProducts", productService.getAllProducts().size());
            model.addAttribute("totalOrders", allOrders.size());

            double totalRevenue = allOrders.stream()
                    .mapToDouble(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0.0)
                    .sum();
            model.addAttribute("totalRevenue", totalRevenue);

            // 2. Doanh thu tháng trước
            LocalDate firstDayLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayLastMonth = LocalDate.now().withDayOfMonth(1).minusDays(1);
            double lastMonthRevenue = allOrders.stream()
                    .filter(o -> o.getOrderDate() != null)
                    .filter(o -> {
                        LocalDate d = o.getOrderDate().toLocalDate();
                        return !d.isBefore(firstDayLastMonth) && !d.isAfter(lastDayLastMonth);
                    })
                    .mapToDouble(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0.0)
                    .sum();
            model.addAttribute("lastMonthRevenue", lastMonthRevenue);

            // 3. Đơn hàng PENDING
            long pendingOrdersCount = allOrders.stream()
                    .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()))
                    .count();
            model.addAttribute("pendingOrdersCount", pendingOrdersCount);

            // 4. Biểu đồ (Labels thực tế)
            List<String> labels = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (int i = days - 1; i >= 0; i--) {
                labels.add(LocalDate.now().minusDays(i).format(formatter));
            }
            model.addAttribute("chartLabels", labels);
            model.addAttribute("revenueData", orderService.getRevenueDataByDays(days));
            model.addAttribute("orderData", orderService.getOrderCountsByDays(days));
            model.addAttribute("selectedDays", days);

            // 5. Best Seller (Top 10)
            List<Object[]> bestSellersData = orderRepository.findTop10BestSellers();
            model.addAttribute("bestSellers", bestSellersData != null ? bestSellersData : new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
            // Đảm bảo các biến model không bị thiếu nếu có lỗi xảy ra
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("lastMonthRevenue", 0.0);
            model.addAttribute("pendingOrdersCount", 0);
            model.addAttribute("bestSellers", new ArrayList<>());
        }
        return "admin/dashboard";
    }

    // ==========================================
    // 2. QUẢN LÝ NGƯỜI DÙNG (USER)
    // ==========================================
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }

    @GetMapping("/users/detail/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/user-detail";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra, Principal principal) {
        User userToDelete = userService.getUserById(id);
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
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/product-list";
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-add";
    }

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

    // ==========================================
    // 4. QUẢN LÝ ĐƠN HÀNG (ORDER)
    // ==========================================
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/order-list";
    }

    @GetMapping("/orders/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "admin/order-detail";
    }

    @PostMapping("/orders/update-status")
    public String updateStatus(@RequestParam Long orderId, @RequestParam String status, RedirectAttributes ra) {
        orderService.updateStatus(orderId, status);
        ra.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        return "redirect:/admin/orders";
    }
}