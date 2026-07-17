package com.panij.GTDistributing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${product.image.upload-dir:/opt/tomcat/uploads/productimg/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ⬇️ Change here
        registry.addResourceHandler("/productimg/**")
                .addResourceLocations("file:" + uploadDir);
    }
}

