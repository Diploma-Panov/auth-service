package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.TokenResponseDto;
import com.mpanov.diploma.auth.dto.UserSignupDto;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_PUBLIC;

@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
public class ServiceUserController {

    private final ServiceUserLogic serviceUserLogic;

    @PostMapping(API_PUBLIC + "/users/signup")
    public AbstractResponseDto<TokenResponseDto> signup(@RequestBody UserSignupDto dto) {
        log.info("User signup: username={}", dto.getUsername());
        TokenResponseDto tokenResponseDto = serviceUserLogic.signupNewUser(dto);
        log.info("Successful user signup: username={}", dto.getUsername());
        return new AbstractResponseDto<>(tokenResponseDto);
    }

}
