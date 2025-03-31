package com.mpanov.diploma.auth.dto.common;

import com.mpanov.diploma.auth.model.SignupErrorType;

public enum ServiceErrorType {
    PLATFORM_ERROR,
    ACCESS_TOKEN_EXPIRED,
    NO_ACCESS_TOKEN_FOUND,
    INVALID_ACCESS_TOKEN,
    LOGIN_FAILED,
    ACCESS_DENIED,
    TOKEN_GENERATION_FAILED,
    EMAIL_IS_INVALID;

   public static ServiceErrorType fromSignupErrorType(SignupErrorType signupErrorType) {
       return switch (signupErrorType) {
           case INVALID_EMAIL_FORMAT -> EMAIL_IS_INVALID;
       };
   }
}
