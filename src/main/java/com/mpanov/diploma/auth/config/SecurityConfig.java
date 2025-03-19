package com.mpanov.diploma.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.model.UserType;
import com.mpanov.diploma.auth.security.*;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sun.net.httpserver.HttpsServer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    public static final String API_VERSION = "/v0";
    public static final String API_PUBLIC = API_VERSION + "/public";
    public static final String API_USER = API_VERSION + "/user";
    public static final String API_ADMIN = API_VERSION + "/admin";

    private final ObjectMapper objectMapper;

    private final CredentialsLoginAuthenticationProvider credentialsLoginAuthenticationProvider;

    private final JwtTransportService jwtTransportService;

    private final HandlerExceptionResolver handlerExceptionResolver;

    private final JwtPayloadService jwtPayloadService;

    private final JwtService jwtService;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    @SneakyThrows
    public AuthenticationManager authenticationManager(HttpSecurity http) {
        var builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(credentialsLoginAuthenticationProvider);
        return builder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setMaxAge(10000L);
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "HEAD", "OPTIONS", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowCredentials(false);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Content-Range", "Content-Length", "ETag"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh ->
                        eh.accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(API_USER + "/**").hasAnyRole(UserType.USER.name())
                        .requestMatchers(API_ADMIN + "/**").hasAnyRole(UserType.ADMIN.name())
                        .requestMatchers(API_PUBLIC + "/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(accessTokenAuthenticationFilter(authenticationManager), ExceptionTranslationFilter.class)
                .addFilter(credentialsAuthenticationFilter(authenticationManager));

        return http.build();
    }

    private CredentialsAuthenticationFilter credentialsAuthenticationFilter(AuthenticationManager authenticationManager) {
        CredentialsAuthenticationFilter filter = new CredentialsAuthenticationFilter(
                API_PUBLIC + "/users/login",
                authenticationManager,
                objectMapper
        );

        filter.setAuthenticationSuccessHandler(userAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(userAuthenticationFailureHandler());

        return filter;
    }

    private AccessTokenAuthenticationFilter accessTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AccessTokenAuthenticationFilter(
                authenticationManager,
                jwtService,
                jwtTransportService
        );
    }

    private UserAuthenticationSuccessHandler userAuthenticationSuccessHandler() {
        return new UserAuthenticationSuccessHandler(jwtTransportService, jwtPayloadService);
    }

    private UserAuthenticationFailureHandler userAuthenticationFailureHandler() {
        return new UserAuthenticationFailureHandler(handlerExceptionResolver);
    }

}
