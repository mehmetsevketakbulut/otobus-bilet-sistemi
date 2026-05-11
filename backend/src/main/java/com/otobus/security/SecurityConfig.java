package com.otobus.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Güvenlik konfigürasyonu.
 * JWT tabanlı stateless authentication, CORS kısıtlaması, XSS koruması,
 * rol bazlı erişim kontrolü ve güvenlik header'ları yapılandırır.
 *
 * NOT: CSRF koruması devre dışı bırakılmıştır çünkü uygulama stateless JWT
 * authentication kullanmaktadır. CSRF saldırıları cookie-based session'lara
 * yönelik olduğundan, JWT Bearer token kullanılan API'lerde CSRF korumasına
 * gerek yoktur.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5500,http://127.0.0.1:5500}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                // CSRF devre dışı: JWT Bearer token kullanıldığı için cookie-based CSRF saldırılarına karşı
                // koruma gerekmez. Bu bilinçli bir tasarım kararıdır.
                .csrf(AbstractHttpConfigurer::disable)
                // Güvenlik header'ları
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(opts -> {})
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                )
                .authorizeHttpRequests(auth -> auth
                        // Herkese açık endpoint'ler
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/trips/search",
                                "/api/cities",
                                "/api/terminals",
                                "/api/trips/*/seats"
                        ).permitAll()
                        // Admin yetkisi gerektiren endpoint'ler
                        .requestMatchers("/api/users/all").hasAuthority("ADMIN")
                        .requestMatchers("/api/tickets/all").hasAuthority("ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/audit-logs/**").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/**")
                                .hasAuthority("ADMIN")
                        // Firma yetkisi gerektiren endpoint'ler
                        .requestMatchers("/api/trips/company/**").hasAuthority("COMPANY")
                        .requestMatchers("/api/company/**").hasAuthority("COMPANY")
                        // Admin firma onaylama
                        .requestMatchers("/api/trips/admin/**").hasAuthority("ADMIN")
                        // Geri kalan tüm istekler authentication gerektirir
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Yetkisiz erişim ve authentication hataları için JSON response döndür
                // Sistem bilgisi sızdırmayan genel hata mesajları kullanılır
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            new ObjectMapper().writeValue(response.getOutputStream(),
                                    Map.of("message", "Oturum geçersiz veya süresi dolmuş. Lütfen tekrar giriş yapın."));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            new ObjectMapper().writeValue(response.getOutputStream(),
                                    Map.of("message", "Bu işlem için yetkiniz bulunmuyor."));
                        })
                );

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Güvenlik: Sadece belirli origin'lere izin ver, "*" yerine kısıtlı liste
        configuration.setAllowedOrigins(java.util.List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
