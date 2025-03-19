package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.ErrorResponseDto;
import com.mpanov.diploma.auth.dto.ErrorResponseElement;
import com.mpanov.diploma.auth.dto.ServiceErrorType;
import com.mpanov.diploma.auth.exception.InvalidTokenException;
import com.mpanov.diploma.auth.exception.LoginException;
import com.mpanov.diploma.auth.exception.PlatformException;
import com.mpanov.diploma.auth.exception.TokenFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static com.mpanov.diploma.auth.security.JwtTransportService.AUTH_HEADER;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @Value("${platform.errors.hide-message}")
    private Boolean hideMessage;

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleAuthenticationException(HttpServletRequest req, AuthenticationException e) {
        logError(req, e);
        ServiceErrorType type;
        if (e instanceof InsufficientAuthenticationException) {
            String authorization = req.getHeader(AUTH_HEADER);
            if (StringUtils.isBlank(authorization)) {
                type = ServiceErrorType.NO_ACCESS_TOKEN_FOUND;
            } else {
                type = ServiceErrorType.INVALID_ACCESS_TOKEN;
            }
        } else if (e instanceof LoginException) {
            type = ServiceErrorType.LOGIN_FAILED;
        } else {
            type = ServiceErrorType.ACCESS_DENIED;
        }
        return this.composeErrorResponseDto(e, type);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleAccessDeniedException(HttpServletRequest req, AuthorizationDeniedException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.ACCESS_DENIED);
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleInvalidTokenException(HttpServletRequest req, InvalidTokenException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.INVALID_ACCESS_TOKEN);
    }

    @ExceptionHandler(TokenFormatException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleTokenFormatException(HttpServletRequest req, TokenFormatException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.INVALID_ACCESS_TOKEN);
    }

    @ExceptionHandler(PlatformException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handlePlatformException(HttpServletRequest req, Exception e) {
        logError(req, e);
        return this.composeErrorResponseDto(e, ServiceErrorType.PLATFORM_ERROR);
    }

    private <E extends Exception> ErrorResponseDto composeErrorResponseDto(E e, ServiceErrorType errorType) {
        ErrorResponseElement singleElement = new ErrorResponseElement(
                hideMessage ? null : e.getMessage(),
                errorType.toString(),
                e.getClass().getSimpleName()
        );
        return new ErrorResponseDto(List.of(singleElement));
    }

    private void logError(HttpServletRequest req, Exception e) {
        String uri = getRequestUri(req);
        String message = getMessage(e);
        log.error("Error on URI: [{}] - {}", uri, message);
    }

    private String getRequestUri(HttpServletRequest req) {
        String forwardReqUri = (String) req.getAttribute("jakarta.servlet.forward.request_uri");
        return StringUtils.isNotBlank(forwardReqUri) ? forwardReqUri : req.getRequestURI();
    }

    private String getMessage(Exception e) {
        if (e == null) return "";
        String className = ClassUtils.getShortClassName(e, null);
        return className + ": " + StringUtils.defaultString(e.getMessage());
    }

}
