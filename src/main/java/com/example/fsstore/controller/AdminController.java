package com.example.fsstore.controller;

import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.Product;
import com.example.fsstore.entity.User;
import com.example.fsstore.repository.OrderRepository;
import com.example.fsstore.repository.UserRepository;
import com.example.fsstore.repository.ProductRepository;
import com.example.fsstore.service.OrderService;
import com.example.fsstore.service.ProductService;
import com.example.fsstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // ==========================================
    // 1. DASHBOARD TỔNG QUÁT
    // ==========================================
    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(name = "days", defaultValue = "7") int days, Model model) {
        try {
            List<Order> allOrders = orderService.getAllOrders();
            model.addAttribute("totalUsers", userService.getAllUsers().size());
            model.addAttribute("totalProducts", productService.getAllProducts().size());
            model.addAttribute("totalOrders", allOrders.size());

            // 1. Tính tổng doanh thu
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

            // 3. Phân tích trạng thái đơn hàng (MỚI)
            long pendingCount = allOrders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
            long confirmedCount = allOrders.stream().filter(o -> "CONFIRMED".equalsIgnoreCase(o.getStatus())).count();
            long shippingCount = allOrders.stream().filter(o -> "SHIPPED".equalsIgnoreCase(o.getStatus())).count();

            model.addAttribute("pendingOrdersCount", pendingCount); // Giữ tên cũ cho thông báo
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("shippingCount", shippingCount);

            // 4. Cảnh báo kho hàng (MỚI)
            // Lấy các sản phẩm có stock < 10, sắp xếp tăng dần
            List<Product> lowStockProducts = productRepository.findByStockLessThanOrderByStockAsc(10);
            model.addAttribute("lowStockProducts", lowStockProducts);

            // 5. Dữ liệu biểu đồ
            List<String> labels = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (int i = days - 1; i >= 0; i--) {
                labels.add(LocalDate.now().minusDays(i).format(formatter));
            }
            model.addAttribute("chartLabels", labels);
            model.addAttribute("revenueData", orderService.getRevenueDataByDays(days));
            model.addAttribute("orderData", orderService.getOrderCountsByDays(days));
            model.addAttribute("selectedDays", days);

            // 6. Best Sellers
            List<Object[]> bestSellersData = orderRepository.findTop10BestSellers();
            model.addAttribute("bestSellers", bestSellersData != null ? bestSellersData : new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("lastMonthRevenue", 0.0);
            model.addAttribute("pendingOrdersCount", 0);
            model.addAttribute("bestSellers", new ArrayList<>());
            model.addAttribute("lowStockProducts", new ArrayList<>());
        }
        return "admin/dashboard";
    }

    // ==========================================
    // 2. QUẢN LÝ NGƯỜI DÙNG (USER)
    // ==========================================
    @GetMapping("/users")
    public String manageUsers(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<User> users;

        // Logic tìm kiếm
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchByKeyword(keyword.trim());
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword); // Gửi lại keyword để hiển thị trên ô input
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
    public String listProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 10; // Bạn có thể thay đổi số lượng hiển thị tại đây

        // Sử dụng phương thức tập trung trong Service để xử lý cả phân trang và tìm kiếm
        Page<Product> productPage = productService.getAllProductsPaged(page, pageSize, keyword);

        model.addAttribute("products", productPage.getContent()); // Danh sách sản phẩm trang hiện tại
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword); // Giữ lại từ khóa trên ô Search

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

    @GetMapping("/products/detail/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        // Sử dụng phương thức findProductById có sẵn trong Service của bạn
        Product product = productService.findProductById(id);

        if (product == null) {
            return "redirect:/admin/products?error=NotFound";
        }
        model.addAttribute("product", product);
        return "admin/product-detail";
    }

    // 1. Hiển thị form chỉnh sửa với dữ liệu cũ
    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findProductById(id);
        if (product == null) {
            return "redirect:/admin/products?error=NotFound";
        }
        model.addAttribute("product", product);
        return "admin/product-edit"; // Chúng ta sẽ tạo file này
    }

    // 2. Xử lý lưu sản phẩm sau khi sửa
    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute("product") Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes ra) {
        try {
            // Lấy sản phẩm cũ từ DB để giữ lại ảnh cũ nếu người dùng không chọn ảnh mới
            Product existingProduct = productService.findProductById(product.getId());

            if (imageFile != null && !imageFile.isEmpty()) {
                // Nếu có upload ảnh mới, dùng phương thức saveProduct có xử lý file
                productService.saveProduct(product, imageFile);
            } else {
                // Nếu không upload ảnh mới, giữ lại tên ảnh cũ
                product.setImageUrl(existingProduct.getImageUrl());
                productService.saveProduct(product); // Lưu trực tiếp object
            }
            ra.addFlashAttribute("message", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            // Đảm bảo sản phẩm tồn tại trước khi xóa (tùy chọn)
            Product product = productService.findProductById(id);
            if (product != null) {
                productService.deleteProduct(id);
                ra.addFlashAttribute("message", "Đã xóa sản phẩm thành công!");
            } else {
                ra.addFlashAttribute("error", "Sản phẩm không tồn tại!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa sản phẩm này! (Có thể do ràng buộc dữ liệu)");
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
    public String updateStatus(@RequestParam Long orderId,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        try {
            orderService.updateStatus(orderId, status);
            ra.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        } catch (RuntimeException e) {
            // Bắt lỗi từ Service (ví dụ: lỗi hết hàng) và gửi thông báo lỗi về giao diện
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            // Quay lại trang chi tiết của chính đơn hàng đó thay vì dừng ở trang trắng
            return "redirect:/admin/orders/detail/" + orderId;
        }
        return "redirect:/admin/orders";
    }
}