package com.mpanov.diploma.auth.config;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;
import static software.amazon.awssdk.regions.Region.US_EAST_1;

@Slf4j
@Data
@Component
public class AwsSecretsConfig {

    @Value("${jwt.secret.jwt-private-key}")
    private String jwtPrivateKey;

    @Value("${jwt.secret.jwt-encryption-secret}")
    private String jwtEncryptionSecret;

    @SneakyThrows
    public static RSAPublicKey parsePublicKey(String publicKey) {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @SneakyThrows
    public static RSAPrivateKey parsePrivateKey(String privateKey) {
        Security.addProvider(new BouncyCastleProvider());
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(US_EAST_1)
                .build();
    }
}
