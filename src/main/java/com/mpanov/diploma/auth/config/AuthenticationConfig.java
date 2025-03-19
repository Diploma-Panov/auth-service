package com.mpanov.diploma.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.security.JwtPayloadService;
import com.mpanov.diploma.auth.security.JwtProperties;
import com.mpanov.diploma.auth.security.JwtService;
import com.mpanov.diploma.auth.security.JwtTransportService;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@AllArgsConstructor
public class AuthenticationConfig {

    private final JwtProperties jwtProperties;

    private final AwsSecretsConfig awsSecretsConfig;

    private final JwtTransportService jwtTransportService;

    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(7);
    }

    @Bean
    public JwtPayloadService jwtPayloadService() {
        return new JwtPayloadService(jwtService(), jwtProperties, jwtTransportService);
    }

    @Bean
    @SneakyThrows
    public JwtService jwtService() {
        // Access token signing
        RSAPublicKey publicKey = AwsSecretsConfig.parsePublicKey(jwtProperties.getPublicKey());
        RSAPrivateKey privateKey = AwsSecretsConfig.parsePrivateKey(awsSecretsConfig.getJwtPrivateKey());
        RSASSASigner signer = new RSASSASigner(privateKey);
        RSASSAVerifier verifier = new RSASSAVerifier(publicKey);

        // Refresh token encryption
        String encryptionSecret = awsSecretsConfig.getJwtEncryptionSecret();
        byte[] secretKey = encryptionSecret.getBytes();
        DirectEncrypter directEncrypter = new DirectEncrypter(secretKey);
        ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<>(secretKey);
        JWEKeySelector<SimpleSecurityContext> jweKeySelector =
                new JWEDecryptionKeySelector<>(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, jweKeySource);
        jwtProcessor.setJWEKeySelector(jweKeySelector);

        return new JwtService(
                directEncrypter,
                signer,
                verifier,
                jwtProcessor,
                objectMapper
        );
    }

}
