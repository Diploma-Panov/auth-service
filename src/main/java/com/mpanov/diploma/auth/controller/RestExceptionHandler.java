package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.exception.*;
import com.mpanov.diploma.data.dto.ErrorResponseDto;
import com.mpanov.diploma.data.dto.ErrorResponseElement;
import com.mpanov.diploma.data.dto.ServiceErrorType;
import com.mpanov.diploma.data.exception.DuplicateException;
import com.mpanov.diploma.data.exception.NotFoundException;
import com.mpanov.diploma.data.exception.PlatformException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest req) {
        logError(req, e);
        return this.composeErrorResponseDto(e, ServiceErrorType.ENTITY_ALREADY_EXISTS);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e);
        return this.composeErrorResponseDto(e, ServiceErrorType.FORM_VALIDATION_FAILED);
    }

    @ExceptionHandler(UserSignupException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleUserSignupException(HttpServletRequest req, UserSignupException e) {
        logError(req, e);
        ServiceErrorType errorType = ServiceErrorType.fromSignupErrorType(e.getErrorType());
        return this.composeErrorResponseDto(e, errorType);
    }

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleMethodArgumentNotValidException(HttpServletRequest req, MethodArgumentNotValidException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.FORM_VALIDATION_FAILED);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleRuntimeException(HttpServletRequest req, RuntimeException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.INTERNAL_ERROR);
    }

    @ExceptionHandler(ShortCodeExpiredException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleShortCodeExpiredException(HttpServletRequest req, ShortCodeExpiredException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.SHORT_CODE_EXPIRED);
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

    @ExceptionHandler(DuplicateException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDuplicateException(HttpServletRequest req, DuplicateException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.ENTITY_ALREADY_EXISTS);
    }

    @ExceptionHandler(OrganizationActionNotAllowed.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto handleOrganizationActionNotAllowed(HttpServletRequest req, OrganizationActionNotAllowed e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotFoundException(HttpServletRequest req, NotFoundException e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.ENTITY_NOT_FOUND);
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
