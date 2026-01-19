package com.example.fsstore.controller;

import com.example.fsstore.entity.Product;
import com.example.fsstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;

    @Autowired
    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String homePage(Model model) {

        // 1. Lấy 4 sản phẩm Nữ có rating cao nhất
        List<Product> femaleFeaturedProducts = productService.findTopRatedProductsByGender("Nu", 4);

        // 2. Lấy 4 sản phẩm Nam có rating cao nhất
        List<Product> maleFeaturedProducts = productService.findTopRatedProductsByGender("Nam", 4);

        // 3. Đặt danh sách Nữ vào Model với tên biến mới
        model.addAttribute("femaleProducts", femaleFeaturedProducts);

        // 4. Đặt danh sách Nam vào Model với tên biến mới
        model.addAttribute("maleProducts", maleFeaturedProducts);

        // Lấy 10 sản phẩm có rating cao nhất
        List<Product> allFeaturedProducts = productService.getTopRatedProducts(10);

        model.addAttribute("featuredProducts", allFeaturedProducts);

        return "home";
    }

}