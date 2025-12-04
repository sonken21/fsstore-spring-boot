package com.example.fsstore.controller;

import com.example.fsstore.entity.Product;
import com.example.fsstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping // (Tùy chọn: nếu bạn muốn thêm tiền tố URL cho tất cả các request trong controller này)
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Hiển thị trang chi tiết sản phẩm.
     * URL: /product/{id}
     * @param id ID của sản phẩm được truyền qua URL
     * @param model Đối tượng Model để truyền dữ liệu sang Thymeleaf
     * @return Tên view (demo6-product.html)
     */
    @GetMapping("/product/{id}")
    public String showProductDetails(@PathVariable("id") Long id, Model model) {

        // 1. Tìm sản phẩm theo ID
        Product product = productService.findProductById(id);

        // 2. Kiểm tra nếu sản phẩm không tồn tại
        if (product == null) {
            // Xử lý trường hợp không tìm thấy sản phẩm (ví dụ: chuyển hướng đến trang lỗi 404)
            // Trong môi trường production, bạn nên throw exception hoặc dùng ControllerAdvice
            return "redirect:/error-404";
        }

        // 3. Đặt đối tượng sản phẩm vào Model
        model.addAttribute("product", product);

        // 4. Trả về tên file view (demo6-product.html)
        return "demo6-product";
    }

    // Bạn có thể thêm các phương thức khác liên quan đến sản phẩm tại đây (ví dụ: /products/search, /products/category...)
}