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
        // Đường dẫn vật lý đến thư mục chứa ảnh
        String reportPath = Paths.get("src/main/resources/static/assets/images/demoes/demo6/products/").toAbsolutePath().toString();

        // Map URL /assets/images/demoes/demo6/products/** vào thư mục vật lý trên
        registry.addResourceHandler("/assets/images/demoes/demo6/products/**")
                .addResourceLocations("file:/" + reportPath + "/");
    }
}