package com.mpanov.diploma.auth.exception;

public class TokenFormatException extends RuntimeException {
    public TokenFormatException(String message) {
        super(message);
    }
}
