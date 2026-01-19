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
        String staticPath = Paths.get("src/main/resources/static/assets/").toFile().getAbsolutePath();
        String protocol = staticPath.startsWith("/") ? "file:" : "file:/";

        registry.addResourceHandler("/assets/**")
                .addResourceLocations(protocol + staticPath + "/")
                .addResourceLocations("classpath:/static/assets/");
    }
}