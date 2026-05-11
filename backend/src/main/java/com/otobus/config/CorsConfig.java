package com.otobus.config;
// Kendi paket adınız neyse (örneğin com.otobus.config) burayı ona göre değiştir kanka

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Dışarıdan gelen isteklere kapıyı açar
        config.addAllowedHeader("*");
        config.addAllowedMethod("*"); // GET, POST, PUT gibi tüm işlemlere izin verir

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}