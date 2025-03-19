package com.mpanov.diploma.auth.dto;

public enum ServiceErrorType {
    PLATFORM_ERROR,
    ACCESS_TOKEN_EXPIRED,
    NO_ACCESS_TOKEN_FOUND,
    INVALID_ACCESS_TOKEN,
    LOGIN_FAILED,
    ACCESS_DENIED,
    TOKEN_GENERATION_FAILED,
}
