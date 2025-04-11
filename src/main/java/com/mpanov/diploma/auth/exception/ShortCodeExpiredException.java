package com.mpanov.diploma.auth.exception;

public class ShortCodeExpiredException extends RuntimeException {
    public ShortCodeExpiredException(String message) {
        super(message);
    }
}
