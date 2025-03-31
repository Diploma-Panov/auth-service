package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.dto.common.TokenResponseDto;
import com.mpanov.diploma.auth.model.common.LoginType;
import com.mpanov.diploma.auth.security.common.JwtUserSubject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JwtPayloadService {

    private final JwtService jwtService;

    private final JwtProperties jwtProperties;

    private final JwtTransportService jwtTransportService;

    public TokenResponseDto getTokensForUserSubject(JwtUserSubject userSubject) {
        String username = userSubject.getUsername();
        LoginType loginType = userSubject.getLoginType();

        Long tokenLifetime = loginType == LoginType.ADMIN_LOGIN ?
                jwtProperties.getAdminTokenLifetime() :
                jwtProperties.getAccessTokenLifetime();
        log.info(
                "getTokensForUserSubject tokenLifetime: {}, username: {}, loginType: {}",
                tokenLifetime,
                username,
                loginType
        );

        String accessToken = jwtService.generateAccessTokenForUserSubject(userSubject, tokenLifetime);

        String refreshToken = null;
        if (userSubject.getLoginType() != LoginType.ADMIN_LOGIN) {
            log.info("getTokensForUserSubject: generating refresh token for username: {}", username);
            userSubject.setOrganizations(null);
            refreshToken = jwtService.generateRefreshTokenForUserSubject(
                    userSubject,
                    jwtProperties.getRefreshTokenLifetime()
            );
        }
        return jwtTransportService.getTokenResponseDto(accessToken, refreshToken);
    }

}
