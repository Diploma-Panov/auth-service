package com.mpanov.diploma.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dto.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JwtTransportService {

    public static final String AUTH_HEADER = "Authorization";

    private static final String ACCESS_TOKEN_PREFIX = "Bearer ";

    private static final String REFRESH_TOKEN_PREFIX = "Refresh ";

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void writeTokenDtoToResponse(TokenResponseDto tokenDto, HttpServletResponse response) {
        objectMapper.writeValue(response.getOutputStream(), new AbstractResponseDto<>(tokenDto));
    }

    public TokenResponseDto getTokenResponseDto(String accessToken, String refreshToken) {
        return new TokenResponseDto(
                ACCESS_TOKEN_PREFIX + accessToken,
                REFRESH_TOKEN_PREFIX + refreshToken
        );
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(ACCESS_TOKEN_PREFIX)) {
            return header.substring(ACCESS_TOKEN_PREFIX.length());
        }
        return null;
    }

}
