package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.data.UserSystemRole;
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
public class SystemTokenAuthenticationFilter extends BasicAuthenticationFilter {

    private final String expectedSystemToken;

    public SystemTokenAuthenticationFilter(AuthenticationManager authenticationManager, String expectedSystemToken) {
        super(authenticationManager);
        this.expectedSystemToken = expectedSystemToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String systemToken = request.getHeader("Authorization");

        if (systemToken == null || !systemToken.startsWith("System ") || !systemToken.substring(7).equals(expectedSystemToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        UserAuthentication authentication = new UserAuthentication(
                JwtUserSubject.builder()
                        .userSystemRole(UserSystemRole.SYSTEM)
                        .build()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
