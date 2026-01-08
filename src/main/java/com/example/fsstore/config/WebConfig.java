package com.example.fsstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Cấu hình cho ảnh Sản phẩm (Giữ nguyên logic của bạn)
        exposeDirectory("src/main/resources/static/assets/images/demoes/demo6/products",
                "/assets/images/demoes/demo6/products/**", registry);

        // 2. Cấu hình cho ảnh Đại diện (Avatar) - MỚI THÊM
        exposeDirectory("src/main/resources/static/assets/images/avatars",
                "/assets/images/avatars/**", registry);
    }

    /**
     * Hàm hỗ trợ ánh xạ đường dẫn thư mục vật lý vào URL Web
     */
    private void exposeDirectory(String dirName, String urlPath, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(dirName);
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Fix lỗi đường dẫn trên Windows (thêm file:/)
        if (uploadPath.startsWith("/")) {
            registry.addResourceHandler(urlPath).addResourceLocations("file:" + uploadPath + "/");
        } else {
            registry.addResourceHandler(urlPath).addResourceLocations("file:/" + uploadPath + "/");
        }
    }
}