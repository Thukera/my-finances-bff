package com.thukera.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000", 
                        "http://192.168.0.60:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true); // ✅ important for cookies
    }
}

