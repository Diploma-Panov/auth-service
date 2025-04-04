package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.common.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoByAdminDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserProfilePictureDto;
import com.mpanov.diploma.auth.dto.user.UserAdminInfoDto;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_ADMIN;

@Slf4j
@RestController
@RequestMapping(API_ADMIN + "/users/{userId}")
@AllArgsConstructor
public class ServiceUserAdminController {

    private final ServiceUserLogic serviceUserLogic;

    private final Mapper mapper;

    @GetMapping("/info")
    public AbstractResponseDto<UserAdminInfoDto> getUserAdminInfo(@PathVariable Long userId) {
        log.info("Received GET /user/users/{}/info", userId);

        ServiceUser user = serviceUserLogic.getServiceUserByIdThrowable(userId);

        UserAdminInfoDto rv = mapper.toUserAdminInfoDto(user);
        return new AbstractResponseDto<>(rv);
    }

    @PatchMapping("/info")
    public AbstractResponseDto<UserAdminInfoDto> updateUserInfoByAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserInfoByAdminDto dto
    ) {
        log.info("Received PATCH /admin/users/{}/info, userId={}", userId, dto);

        ServiceUser updatedUser = serviceUserLogic.updateUserInfoByAdmin(userId, dto);

        UserAdminInfoDto rv = mapper.toUserAdminInfoDto(updatedUser);
        return new AbstractResponseDto<>(rv);
    }

    @PutMapping("/profile-picture")
    public AbstractResponseDto<UserAdminInfoDto> updateProfilePictureByAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfilePictureDto dto
    ) {
        log.info("Received PUT /admin/users/{}/profile-picture", userId);
        ServiceUser user = serviceUserLogic.getServiceUserByIdThrowable(userId);
        ServiceUser updatedUser = serviceUserLogic.updateProfilePicture(user, dto.getNewProfilePictureBase64());
        UserAdminInfoDto rv = mapper.toUserAdminInfoDto(updatedUser);
        return new AbstractResponseDto<>(rv);
    }

    @DeleteMapping("/profile-picture")
    public AbstractResponseDto<UserAdminInfoDto> deleteProfilePictureByAdmin(
            @PathVariable Long userId
    ) {
        log.info("Received DELETE /admin/users/{}/profile-picture", userId);
        ServiceUser user = serviceUserLogic.getServiceUserByIdThrowable(userId);
        ServiceUser updatedUser = serviceUserLogic.removeProfilePicture(user);
        UserAdminInfoDto rv = mapper.toUserAdminInfoDto(updatedUser);
        return new AbstractResponseDto<>(rv);
    }

}
