package com.fyp.HostelMate.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Serve uploaded files from /uploads/** URL
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    // It also covers WebMvcConfigurer CORS for non-security routes.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow the Vite dev server and any future production domain
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        // Allow all standard HTTP methods including OPTIONS (preflight)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers including Authorization and Content-Type
        config.setAllowedHeaders(List.of("*"));

        // Allow Authorization header to be exposed to the frontend
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies/credentials
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces preflight requests)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to every path
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
