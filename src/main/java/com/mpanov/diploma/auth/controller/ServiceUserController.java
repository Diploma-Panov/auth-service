package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.common.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.common.TokenResponseDto;
import com.mpanov.diploma.auth.dto.user.*;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.common.ActorContext;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.mpanov.diploma.auth.config.SecurityConfig.*;

@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
public class ServiceUserController {

    private final ServiceUserLogic serviceUserLogic;

    private final ServiceUserDao serviceUserDao;

    private final ActorContext actorContext;

    private final Mapper mapper;

    @PostMapping(API_PUBLIC + "/users/signup")
    public AbstractResponseDto<TokenResponseDto> signup(@RequestBody UserSignupDto dto) {
        log.info("User signup: username={}", dto.getUsername());
        TokenResponseDto tokenResponseDto = serviceUserLogic.signupNewUser(dto);
        log.info("Successful user signup: username={}", dto.getUsername());
        return new AbstractResponseDto<>(tokenResponseDto);
    }

    @GetMapping(API_USER + "/personal-info")
    public AbstractResponseDto<UserInfoDto> getPersonalInfo() {
        ServiceUser actorUser = actorContext.getAuthenticatedUser();
        log.info("Received GET /user/personal-info, by userId={}", actorUser.getId());
        UserInfoDto rv = mapper.toUserInfoDto(actorUser);
        return new AbstractResponseDto<>(rv);
    }

    @PatchMapping(API_USER + "/personal-info")
    public AbstractResponseDto<UserInfoDto> updateUserInfo(@RequestBody UpdateUserInfoDto dto) {
        ServiceUser actorUser = actorContext.getAuthenticatedUser();
        Long userId = actorUser.getId();

        log.info("Received PATCH /user/personal-info, with dto={}, for userId={}", dto, userId);

        ServiceUser updatedUser = serviceUserLogic.updateUserInfo(actorUser, dto);

        UserInfoDto rv = mapper.toUserInfoDto(updatedUser);
        return new AbstractResponseDto<>(rv);
    }

    @GetMapping(API_ADMIN + "/users/{userId}/info")
    public AbstractResponseDto<UserAdminInfoDto> getUserAdminInfo(@PathVariable Long userId) {
        log.info("Received GET /user/users/{}/info", userId);

        ServiceUser user = serviceUserDao.getServiceUserByIdThrowable(userId);

        UserAdminInfoDto rv = mapper.toUserAdminInfoDto(user);
        return new AbstractResponseDto<>(rv);
    }

    @PatchMapping(API_ADMIN + "/users/{userId}/info")
    public AbstractResponseDto<UserAdminInfoDto> updateUserInfoByAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserInfoByAdminDto dto
    ) {
        log.info("Received PATCH /admin/users/{}/info, userId={}", userId, dto);

        ServiceUser updatedUser = serviceUserLogic.updateUserInfoByAdmin(userId, dto);

        UserAdminInfoDto rv = mapper.toUserAdminInfoDto(updatedUser);
        return new AbstractResponseDto<>(rv);
    }

}
