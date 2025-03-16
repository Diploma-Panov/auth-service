package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.HealthResponseDto;
import com.mpanov.diploma.auth.exception.PlatformException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_PUBLIC;

@RestController
@RequestMapping(API_PUBLIC + "/platform")
public class PlatformController {

    @GetMapping("/health")
    public AbstractResponseDto<HealthResponseDto> platformHealth() {
        return new AbstractResponseDto<>(HealthResponseDto.up());
    }

    @GetMapping("/error")
    public void platformError() {
        throw new PlatformException("Demo platform error");
    }

}
