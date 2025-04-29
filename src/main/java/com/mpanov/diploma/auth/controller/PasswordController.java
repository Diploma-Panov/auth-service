package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.user.ResetPasswordDto;
import com.mpanov.diploma.auth.dto.user.SendResetPasswordDto;
import com.mpanov.diploma.auth.exception.TokenExpiredException;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.service.CacheService;
import com.mpanov.diploma.auth.service.EmailService;
import com.mpanov.diploma.data.dto.AbstractResponseDto;
import com.mpanov.diploma.data.dto.MessageResponseDto;
import com.mpanov.diploma.data.security.PasswordService;
import com.mpanov.diploma.utils.RandomUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_PUBLIC;

@Slf4j
@Controller
@RequestMapping(API_PUBLIC + "/users")
@RequiredArgsConstructor
public class PasswordController {

    private static final String KEY_PREFIX = "reset-password-code-";

    private final CacheService cacheService;

    private final EmailService emailService;

    private final ServiceUserDao serviceUserDao;

    private final PasswordService passwordService;

    @Value("${shortener.base-url}")
    private String shortenerBaseUrl;

    @PostMapping(value = "/send-reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AbstractResponseDto<MessageResponseDto> sendResetPassword(@Valid @RequestBody SendResetPasswordDto dto) {
        log.info("Reset password request received for email {}", dto.getEmail());
        try {
            String resetCode = RandomUtils.generateRandomAlphabeticalString(10);
            cacheService.cacheWithTTL(KEY_PREFIX + resetCode, dto.getEmail(), Duration.ofHours(1));
            ServiceUser user = serviceUserDao.getServiceUserByEmailThrowable(dto.getEmail());
            emailService.sendPasswordRecoveryEmail(dto.getEmail(), user.getFirstname(), resetCode);
        } catch (Exception e) {
            log.error("Could not send reset password email", e);
        }
        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        log.info("Resetting password for code {}", dto.getRecoveryCode());

        String email = cacheService.getValue(KEY_PREFIX + dto.getRecoveryCode());
        if (email == null) {
            throw new TokenExpiredException("Password reset token " + dto.getRecoveryCode() + " has expired");
        }
        cacheService.deleteValue(KEY_PREFIX + dto.getRecoveryCode());

        String newPasswordHash = passwordService.encryptPassword(dto.getNewPassword());
        ServiceUser user = serviceUserDao.getServiceUserByEmailThrowable(email);
        user.setPasswordHash(newPasswordHash);

        serviceUserDao.syncInfo(user);

        return "redirect:" + shortenerBaseUrl;
    }

}
