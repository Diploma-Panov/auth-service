package com.mpanov.diploma.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@AllArgsConstructor
public class UserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) {
        String currentUrl = req.getRequestURI();
        log.warn("onAuthenticationFailure url={}", currentUrl, e);
        handlerExceptionResolver.resolveException(req, res, null, e);
    }

}
