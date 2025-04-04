package com.mpanov.diploma.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class LoginException extends AuthenticationException {

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
      super(message, cause);
    }

}
