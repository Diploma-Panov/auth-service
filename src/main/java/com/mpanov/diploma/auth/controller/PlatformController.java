package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.data.dto.AbstractResponseDto;
import com.mpanov.diploma.data.dto.HealthResponseDto;
import com.mpanov.diploma.data.dto.MessageResponseDto;
import com.mpanov.diploma.data.exception.PlatformException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mpanov.diploma.auth.config.SecurityConfig.*;

@RestController
@RequestMapping
public class PlatformController {

    @GetMapping(API_PUBLIC + "/platform/health")
    public AbstractResponseDto<HealthResponseDto> platformHealth() {
        return new AbstractResponseDto<>(HealthResponseDto.up());
    }

    @GetMapping(API_PUBLIC + "/platform/error")
    public void platformError() {
        throw new PlatformException("Demo platform error");
    }

    @GetMapping(API_USER + "/platform/user-auth")
    public AbstractResponseDto<MessageResponseDto> userAuthCheck() {
        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

    @GetMapping(API_ADMIN + "/platform/admin-auth")
    public AbstractResponseDto<MessageResponseDto> adminAuthCheck() {
        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

}
