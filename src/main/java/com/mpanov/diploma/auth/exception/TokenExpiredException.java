package com.mpanov.diploma.auth.exception;

import org.springframework.security.authorization.AuthorizationDeniedException;

public class TokenExpiredException extends AuthorizationDeniedException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
