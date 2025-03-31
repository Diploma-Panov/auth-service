package com.mpanov.diploma.auth.exception.common;

public class JwtVerificationException extends RuntimeException {
  public JwtVerificationException(String message) {
    super(message);
  }
}
