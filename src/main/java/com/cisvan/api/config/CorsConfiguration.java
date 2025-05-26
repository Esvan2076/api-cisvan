package com.cisvan.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer { // Considera renombrar esta clase para evitar confusión con org.springframework.web.cors.CorsConfiguration

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
              .allowedOrigins("https://zparklabs.com", "http://localhost:5173")
              .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Asegúrate de incluir todos los métodos que usas (PATCH es común)
              .allowedHeaders("*")
              .allowCredentials(true);
    }
}