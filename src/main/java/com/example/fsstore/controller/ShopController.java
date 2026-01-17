package com.example.fsstore.controller;

import com.example.fsstore.entity.Product;
import com.example.fsstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShopController {

    private final ProductService productService;

    @Autowired
    public ShopController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Xử lý request cho Trang Danh mục Sản phẩm (Shop Page)
     * Hỗ trợ phân trang, tìm kiếm, và lọc theo Category/Gender/Giá.
     */
    @GetMapping("/shop")
    public String showShopPage(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,

            // Tham số tìm kiếm
            @RequestParam(value = "keyword", required = false) String keyword,

            // Tham số lọc Category/Gender/Type
            @RequestParam(value = "cat", required = false) String catValue,

            // THAM SỐ LỌC GIÁ MỚI
            @RequestParam(value = "min_price", required = false) Double minPrice,
            @RequestParam(value = "max_price", required = false) Double maxPrice,

            Model model) {

        // 1. GỌI SERVICE với TẤT CẢ 5 tham số lọc/phân trang
        Page<Product> productPage = productService.findPaginatedProducts(
                pageable, keyword, catValue, minPrice, maxPrice
        );

        // 2. Đưa đối tượng Page<Product> và nội dung vào Model
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());

        // 3. Đưa thông tin phân trang tiện ích vào Model
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());

        // 4. TRUYỀN CÁC THAM SỐ LỌC/TÌM KIẾM VÀO MODEL
        model.addAttribute("keyword", keyword);
        model.addAttribute("catValue", catValue);
        model.addAttribute("minPrice", minPrice); // <-- THÊM
        model.addAttribute("maxPrice", maxPrice); // <-- THÊM

        return "shop";
    }
}