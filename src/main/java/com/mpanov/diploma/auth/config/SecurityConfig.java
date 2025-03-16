package com.mpanov.diploma.auth.config;

import com.mpanov.diploma.auth.model.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String API_VERSION = "/v0";
    public static final String API_PUBLIC = API_VERSION + "/public";
    public static final String API_USER = API_VERSION + "/user";
    public static final String API_ADMIN = API_VERSION + "/admin";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(API_USER + "/**").hasAnyRole(UserRole.USER.name())
                        .requestMatchers(API_ADMIN + "/**").hasAnyRole(UserRole.ADMIN.name())
                        .requestMatchers(API_PUBLIC + "/**", "/error").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}
