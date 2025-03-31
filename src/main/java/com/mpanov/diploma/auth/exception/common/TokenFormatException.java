package com.mpanov.diploma.auth.exception.common;

public class TokenFormatException extends RuntimeException {
    public TokenFormatException(String message) {
        super(message);
    }
}
