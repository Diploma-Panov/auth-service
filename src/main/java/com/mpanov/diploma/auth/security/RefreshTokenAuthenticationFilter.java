package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.dto.TokenResponseDto;
import com.mpanov.diploma.auth.exception.InvalidTokenException;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtTransportService jwtTransportService;

    private final JwtPayloadService jwtPayloadService;

    private final JwtService jwtService;

    private final ServiceUserLogic serviceUserLogic;

    public RefreshTokenAuthenticationFilter(
            String path,
            JwtTransportService jwtTransportService,
            JwtPayloadService jwtPayloadService,
            JwtService jwtService,
            ServiceUserLogic serviceUserLogic
    ) {
        super(new AntPathRequestMatcher(path, "GET"));
        this.jwtTransportService = jwtTransportService;
        this.jwtPayloadService = jwtPayloadService;
        this.jwtService = jwtService;
        this.serviceUserLogic = serviceUserLogic;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String refreshToken = jwtTransportService.getRefreshTokenFromRequest(request);

       if (refreshToken == null) {
           throw new InvalidTokenException("No refresh token provided");
       }

        JwtUserSubject oldSubject = jwtService.getRefreshUserSubject(refreshToken);

       JwtUserSubject newSubject = serviceUserLogic.refreshSubject(oldSubject);

        return new UserAuthentication(newSubject);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        UserAuthentication userAuthentication = (UserAuthentication) authResult;
        JwtUserSubject subject = userAuthentication.getUserSubject();
        TokenResponseDto tokenDto = jwtPayloadService.getTokensForUserSubject(subject);

        jwtTransportService.writeTokenDtoToResponse(tokenDto, response);
    }
}
