package com.mpanov.diploma.auth.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.exception.TokenFormatException;
import com.mpanov.diploma.auth.exception.TokenGenerationException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.maven.shared.utils.StringUtils;

import java.text.ParseException;
import java.util.Date;

@AllArgsConstructor
public class JwtService {

    public static final String JWT_AUDIENCE = "aud";

    private final DirectEncrypter encrypter;

    private final RSASSASigner signer;

    private final RSASSAVerifier verifier;

    private final ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor;

    private final ObjectMapper mapper;

    public JwtUserSubject getAccessUserSubject(String token) {
        JWTClaimsSet claims = this.parseAccessToken(token);
        return getSubjectFromClaims(claims);
    }

    public JwtUserSubject getRefreshUserSubject(String token) {
        JWTClaimsSet claims = this.parseRefreshToken(token);
        return getSubjectFromClaims(claims);
    }

    @SneakyThrows
    public String generateAccessTokenForUserSubject(JwtUserSubject subject, Long expirationTime) {
        Payload payload = createPayload(subject, expirationTime);
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS512), payload);
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }

    @SneakyThrows
    public String generateRefreshTokenForUserSubject(JwtUserSubject subject, Long expirationTime) {
        Payload payload = createPayload(subject, expirationTime);
        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512);
        JWEObject jweObject = new JWEObject(header, payload);
        jweObject.encrypt(encrypter);
        return jweObject.serialize();
    }

    @SneakyThrows
    private JWTClaimsSet parseAccessToken(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            if (!jwsObject.verify(verifier)) {
                throw new TokenGenerationException("Invalid JWT signature");
            }
            JWTClaimsSet claims = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
            if (!claims.getAudience().contains(JWT_AUDIENCE)) {
                throw new TokenGenerationException("Invalid JWT audience");
            }

            long expirationTime = claims.getExpirationTime().getTime();
            long currentTime = System.currentTimeMillis();

            if (expirationTime < currentTime) {
                throw new TokenGenerationException("JWT expired");
            }

            return claims;
        } catch (JOSEException | ParseException e) {
            throw new TokenGenerationException(e.getMessage());
        }
    }

    @SneakyThrows
    private JWTClaimsSet parseRefreshToken(String token) {
        return jwtProcessor.process(token, null);
    }

    private Payload createPayload(JwtUserSubject subject, Long expirationTime) throws JsonProcessingException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(System.currentTimeMillis() + expirationTime))
                .audience(JWT_AUDIENCE)
                .subject(mapper.writeValueAsString(subject))
                .issueTime(new Date())
                .issuer("Maksym Panov")
                .build();
        return new Payload(claims.toJSONObject());
    }

    @SneakyThrows
    private JwtUserSubject getSubjectFromClaims(JWTClaimsSet claims) {
        String userClaim = claims.getSubject();
        if (StringUtils.isBlank(userClaim)) {
            throw new TokenFormatException("Missing JWT subject claim");
        }
        return mapper.readValue(userClaim, JwtUserSubject.class);
    }

}
