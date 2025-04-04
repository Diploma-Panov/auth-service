package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.exception.LoginException;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.security.JwtUserSubject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Slf4j
@AllArgsConstructor
public class UserAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTransportService jwtTransportService;

    private final JwtPayloadService jwtPayloadService;

    @Override
    @SneakyThrows(LoginException.class)
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);

        long startTime = System.currentTimeMillis();

        UserAuthentication userAuthentication = (UserAuthentication) auth;
        JwtUserSubject subject = userAuthentication.getUserSubject();

        TokenResponseDto tokenDto = jwtPayloadService.getTokensForUserSubject(subject);

        jwtTransportService.writeTokenDtoToResponse(tokenDto, res);

        long endTime = System.currentTimeMillis();
        log.info("Access token generation time - {} ms", endTime - startTime);
    }


}
