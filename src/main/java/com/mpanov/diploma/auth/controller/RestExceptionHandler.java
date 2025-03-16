package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.ErrorResponseDto;
import com.mpanov.diploma.auth.dto.ErrorResponseElement;
import com.mpanov.diploma.auth.dto.ServiceErrorType;
import com.mpanov.diploma.auth.exception.PlatformException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @Value("${platform.errors.hide-message}")
    private Boolean hideMessage;

    @ExceptionHandler(PlatformException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handlePlatformException(HttpServletRequest req, Exception e) {
        logError(req, e);
        return composeErrorResponseDto(e, ServiceErrorType.PLATFORM_ERROR);
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
