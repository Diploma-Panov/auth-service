package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.common.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.user.ChangeUserSystemRoleByAdminDto;
import com.mpanov.diploma.auth.dto.common.MessageResponseDto;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_ADMIN;

@Slf4j
@RestController
@RequestMapping(API_ADMIN + "/users")
@AllArgsConstructor
public class AdminUserController {

    private final ServiceUserLogic serviceUserLogic;

    @PatchMapping("/role")
    public AbstractResponseDto<MessageResponseDto> changeUserSystemRoleByAdmin(@Valid @RequestBody ChangeUserSystemRoleByAdminDto dto) {
        log.info("changeUserSystemRoleByAdmin: {}", dto);
        serviceUserLogic.changeUserSystemRole(dto.getUserId(), dto.getNewRole());
        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

}
