package com.cisvan.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cisvan.api.services.MyUserDetailsService;

import java.util.List;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final MyUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Usará el bean de abajo
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/me",
                                "/user/upload-image",
                                "/user/profile",
                                "/user/profile-image",
                                "/user/update-image-url",
                                "/user/followers", // Nota: Este también está en permitAll, revisa la lógica
                                "/user-list/**",
                                "/notifications/**",
                                "/comments", // Duplicado, una vez es suficiente
                                "/comments-like/**",
                                "/comments/{commentId}/report",
                                "/comments/reply",
                                "/user/activate-notification",
                                "/user/deactivate-notification",
                                "/user/notification-prompt-status",
                                "/reviews/submit")
                        .authenticated()
                        .requestMatchers(
                                "/comments/admin/**",
                                "/user/{userId}/ban-toggle",
                                "/user/banned")
                        .hasRole("ADMIN")

                        .requestMatchers( // Estos son los endpoints públicos
                                "/auth/**", // Es común tener un path base para autenticación como /auth/login,
                                            // /auth/register
                                "/user/register", // Asumiendo que tienes un endpoint de registro
                                "/user/login", // Asumiendo que tienes un endpoint de login
                                "/user/resend-code",
                                "/user/verify-email",
                                "/user/forgot-password",
                                "/user/reset-password")
                        .permitAll()
                        .anyRequest().permitAll() // CAMBIO IMPORTANTE: Si quieres que todo lo demás sea público por
                                                  // defecto.
                                                  // Si quieres que todo lo demás requiera autenticación por defecto,
                                                  // usa .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable()) // Si usas JWT, basic auth no es necesario
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://zparklabs.com", "http://localhost:5173/")); // ✅ AQUÍ el dominio
                                                                                              // correcto
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
