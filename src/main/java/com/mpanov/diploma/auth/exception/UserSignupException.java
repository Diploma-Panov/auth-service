package com.mpanov.diploma.auth.exception;

import com.mpanov.diploma.data.SignupErrorType;
import lombok.Getter;

@Getter
public class UserSignupException extends RuntimeException {

    private final SignupErrorType errorType;

    public UserSignupException(SignupErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
