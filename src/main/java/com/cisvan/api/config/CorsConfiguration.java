package com.cisvan.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // Permite todos los orígenes
                        .allowedMethods("*") // Permite todos los métodos HTTP
                        .allowedHeaders("*") // Permite todos los headers
                        .allowCredentials(false); // Las credenciales NO pueden enviarse con "*"
            }
        };
    }    
}