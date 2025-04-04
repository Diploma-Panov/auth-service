package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.data.security.JwtUserSubject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class AccessTokenAuthenticationFilter extends BasicAuthenticationFilter {

    private final JwtService jwtService;

    private final JwtTransportService jwtTransportService;

    public AccessTokenAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtTransportService jwtTransportService
    ) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.jwtTransportService = jwtTransportService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtTransportService.getAccessTokenFromRequest(request);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtUserSubject userSubject = jwtService.getAccessUserSubject(accessToken);
        UserAuthentication authentication = new UserAuthentication(userSubject);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
