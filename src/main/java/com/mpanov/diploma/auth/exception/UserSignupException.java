package com.mpanov.diploma.auth.exception;

import com.mpanov.diploma.auth.model.SignupErrorType;

public class UserSignupException extends RuntimeException {

    private SignupErrorType errorType;

    public UserSignupException(SignupErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
