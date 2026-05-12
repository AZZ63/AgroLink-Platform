package com.agrolink.productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * <p>
 * 配置静态资源映射，将上传目录映射为可访问的 URL 路径，
 * 使得前端可以通过 /uploads/** 访问上传的文件。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 文件上传目录，默认值为 "uploads"，可在配置文件中覆盖 */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 的请求映射到文件系统中的上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
