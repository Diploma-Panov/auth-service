package com.mpanov.diploma.auth.exception;

public class JwtVerificationException extends RuntimeException {
  public JwtVerificationException(String message) {
    super(message);
  }
}
