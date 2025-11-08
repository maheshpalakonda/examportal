package com.exam.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // This configuration applies to all API endpoints under /api/
                registry.addMapping("/api/**") 
                        // Allow requests from your Angular application's origin
                        .allowedOrigins("http://localhost:4200","http://72.60.219.208:8083")
                        // Allow these specific HTTP methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") 
                        // Allow all headers (including the Authorization header)
                        .allowedHeaders("*") 
                        // Allow credentials (like the Authorization header) to be sent
                        .allowCredentials(true);
            }
        };
    }
}
